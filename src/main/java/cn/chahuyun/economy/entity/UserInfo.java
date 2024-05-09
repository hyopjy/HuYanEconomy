package cn.chahuyun.economy.entity;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.contact.User;

import java.io.Serializable;
import java.util.*;

/**
 * 用户信息<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 9:45
 */
@Entity(name = "UserInfo")
@Table
@Getter
@Setter
public class UserInfo implements Serializable {


    @Id
    private String id;
    /**
     * qq号
     */
    private long qq;
    /**
     * 名称
     */
    private String name;
    /**
     * 注册群号
     */
    private long registerGroup;
    /**
     * 注册时间
     */
    private Date registerTime;
    /**
     * 签到状态
     */
    private boolean sign;
    /**
     * 签到时间
     */
    private Date signTime;
    /**
     * 连续签到次数
     */
    private int signNumber = 0;
    /**
     * 断掉的连续签到次数
     */
    private int oldSignNumber;
    /**
     * 签到收益
     */
    private double signEarnings;
    /**
     * 银行收益
     */
    private double bankEarnings;

    /**
     * 用户rgb属性
     */
    private String rgb;

    /**
     * 道具背包
     */
    @OneToMany(targetEntity = UserBackpack.class, mappedBy = "userId", fetch = FetchType.EAGER)
    private List<UserBackpack> backpacks;
    /**
     * 称号信息
     */
    @OneToMany(targetEntity = TitleInfo.class, mappedBy = "userId", fetch = FetchType.EAGER)
    private List<TitleInfo> titleInfos;

    @Transient
    private User user;

    public UserInfo() {
    }

    public UserInfo(long qq, long registerGroup, String name, Date registerTime) {
        this.id = new AbstractPermitteeId.ExactUser(qq).asString();
        this.qq = qq;
        this.registerGroup = registerGroup;
        this.name = name;
        this.registerTime = registerTime;
        this.rgb = Constant.RGB_LIST.get(RandomUtil.randomInt(0,3));
    }


    /**
     * 签到
     *
     * @return boolean true 签到成功 false 签到失败
     * @author Moyuyanli
     * @date 2022/11/14 10:16
     */
    public boolean sign() {
//        if (true) return true;
        //如果签到时间为空->新用户 第一次签到
        if (this.getSignTime() == null) {
            this.setSign(true);
            this.setSignTime(new Date());
            this.setSignNumber(1);
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
            return true;
        }
        //获取签到时间，向后偏移一天
        Calendar calendar = CalendarUtil.calendar(DateUtil.offsetDay(getSignTime(), 1));
        //设置时间为 04:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date time = calendar.getTime();
        //获取小时数差
        long between = DateUtil.between(time, new Date(), DateUnit.MINUTE, false);
        Log.debug("账户:(" + this.getQq() + ")签到时差->" + between);
        //时间还在24小时之内  则为负数
        if (between < 0) {
            return false;
        } else if (between <= 1440) {
            this.setSignNumber(this.getSignNumber() + 1);
            this.setOldSignNumber(0);
        } else {
            this.setOldSignNumber(this.getSignNumber());
            this.setSignNumber(1);
        }
        this.setSign(true);
        this.setSignTime(new Date());
        HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        return true;
    }

    /**
     * 将这个道具添加到用户背包<p>
     *
     * @param userBackpack 背包格信息
     * @return boolean  true 成功
     * @author Moyuyanli
     * @date 2022/11/28 15:55
     */
    public boolean addPropToBackpack(UserBackpack userBackpack) {
        try {
            HibernateUtil.factory.fromTransaction(session -> session.merge(userBackpack));
//            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            Log.error("用户信息:添加道具到背包出错", e);
            return false;
        }
        return true;
    }


    public boolean isSign() {
        String now = DateUtil.format(new Date(), "yyyy-MM-dd") + " 04:00:00";
        DateTime nowDate = DateUtil.parse(now);
        long between = DateUtil.between(nowDate, signTime, DateUnit.HOUR, false);
        return between > 0;
    }

    /**
     * 获取钓鱼信息<p>
     * 不存在则注册一个<p>
     *
     * @return FishInfo 钓鱼信息
     * @see FishInfo
     */
    public FishInfo getFishInfo() {
        FishInfo fishInfo;
        try {
            fishInfo = HibernateUtil.factory.fromSession(session -> session.get(FishInfo.class, this.getQq()));
            if (fishInfo != null) return fishInfo;
        } catch (Exception ignored) {
        }
        FishInfo newFishInfo = new FishInfo(this.getQq(), this.getRegisterGroup());
        return HibernateUtil.factory.fromTransaction(session -> session.merge(newFishInfo));
    }

    public String getString() {
        return "用户名称:" + this.getName() +
                "\n用户qq:" + this.getQq() +
                "\n连续签到:" + this.getSignNumber() + "天\n";
    }

    /**
     * 保存
     *
     * @return 本身
     * @author Moyuyanli
     * @date 2022/12/6 8:49
     */
    public UserInfo save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    /**
     * 设置user
     *
     * @param user 用户
     */
    public UserInfo setUser(User user) {
        this.user = user;
        return this;
    }

    public void setQq(long qq) {
        this.qq = qq;
    }


}
