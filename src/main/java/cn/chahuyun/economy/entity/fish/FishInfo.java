package cn.chahuyun.economy.entity.fish;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.io.Serializable;

/**
 * 钓鱼信息-玩家<p>
 *
 * @author Moyuyanli
 * @date 2022/12/8 10:48
 */
@Table
@Entity(name = "FishInfo")
@Getter
@Setter
public class FishInfo implements Serializable {

    @Id
    private long id;
    /**
     * 所属玩家
     */
    private long qq;
    /**
     * 是否购买鱼竿
     */
    private boolean fishRod;
    /**
     * 是否在钓鱼
     */
    private boolean status;
    /**
     * 鱼竿等级
     */
    private int rodLevel;
    /**
     * 默认鱼塘
     */
    private String defaultFishPond;

    public FishInfo() {
    }

    /**
     * 创建钓鱼玩家<p>
     *
     * @param qq    玩家qq
     * @param group 默认鱼塘
     */
    public FishInfo(long qq, long group) {
        this.id = qq;
        this.qq = qq;
        this.fishRod = false;
        this.rodLevel = 0;
        this.defaultFishPond = "g-" + group;
    }

    /**
     * 保存
     */
    public FishInfo save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    /**
     * 升级鱼竿<p>
     *
     * @param userInfo 用户信息
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli
     * @date 2022/12/8 10:59
     */
    public SingleMessage updateRod(UserInfo userInfo) {
        User user = userInfo.getUser();
        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        int upMoney = 1;
        if (getRodLevel() == 0) {
            return isMoney(user, moneyByUser, upMoney);
        } else if (1 <= getRodLevel() && getRodLevel() < 70) {
            upMoney = 40 * getRodLevel() * getLevel();
            return isMoney(user, moneyByUser, upMoney);
        } else if (70 <= getRodLevel() && getRodLevel() < 80) {
            upMoney = 80 * getRodLevel() * getLevel();
            return isMoney(user, moneyByUser, upMoney);
        } else if (80 <= getRodLevel() && getRodLevel() < 90) {
            upMoney = 100 * getRodLevel() * getLevel();
            return isMoney(user, moneyByUser, upMoney);
        } else if (90 <= getRodLevel() && getRodLevel() < 100) {
            upMoney = 150 * getRodLevel() * getLevel();
            return isMoney(user, moneyByUser, upMoney);
        } else if (getRodLevel() == 99) {
            upMoney = 150000;
            return isMoney(user, moneyByUser, upMoney);
        } else {
            return new PlainText("你的鱼竿已经满级拉！");
        }
    }

    /**
     * 获取默认鱼塘<p>
     *
     * @author Moyuyanli
     * @date 2022/12/8 15:11
     * @see FishPond
     */
    public FishPond getFishPond() {
        FishPond fishPond;
        try {
            //从数据库中查询该鱼塘
            fishPond = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<FishPond> query = builder.createQuery(FishPond.class);
                JpaRoot<FishPond> from = query.from(FishPond.class);
                query.select(from);
                query.where(builder.equal(from.get("code"), this.getDefaultFishPond()));
                return session.createQuery(query).getSingleResult();
            });
            //如果不存在 或者报错，则进行新建改鱼塘
            if (fishPond != null) {
                return fishPond;
            }
        } catch (Exception e) {
            Log.debug(e);
        }
        //分割鱼塘id信息
        String[] split = this.getDefaultFishPond().split("-");
        //如果为2 -> 群默认鱼塘格式  g-(群号)
        if (split.length == 2) {
            long group = Long.parseLong(split[1]);
            Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
            if (bot == null) {
                return null;
            }
            Group botGroup = bot.getGroup(group);
            assert botGroup != null;
            //注册新鱼塘
            FishPond finalFishPond = new FishPond(1, group, HuYanEconomy.config.getOwner(), botGroup.getName() + "鱼塘", "「纯天然湖泊，鱼情优秀，又大又多」，但据内部人士爆料，这是黑心土著挖的人工湖");
            return HibernateUtil.factory.fromTransaction(session -> session.merge(finalFishPond));
        } else {
            //todo 私人鱼塘
            return null;
        }
    }

    /**
     * 获取钓鱼的鱼竿支持最大等级
     *
     * @return 鱼竿支持最大等级
     */
    public int getLevel() {
        return getRodLevel() == 0 ? 1 : getRodLevel() / 4 + 2;
    }

    /**
     * 鱼竿等级+1
     */
    private void upFishRod() {
        int upRod = getRodLevel();
        this.setRodLevel(upRod + 1);
        save();
    }

    public void downFishRod() {
        int upRod = getRodLevel();
        this.setRodLevel(upRod - 2 < 0 ? 1 : upRod - 2);
        save();
    }

    /**
     * 相同的升级
     *
     * @param user      用户
     * @param userMoney 用户拥有的金币
     * @param upMoney   升级鱼竿的金币
     * @return 成功消息
     */
    private synchronized SingleMessage isMoney(User user, double userMoney, int upMoney) {
        if (userMoney - upMoney < 0) {
            return new PlainText(String.format("😣你的币币不够%s啦！", upMoney));
        }
        if (EconomyUtil.minusMoneyToUser(user, upMoney)) {
            upFishRod();
            return new PlainText(String.format("升级成功,花费%sWDIT币币!你的鱼竿更强了!\n%s->%s", upMoney, this.getRodLevel() - 1, getRodLevel()));
        }
        return new PlainText("升级失败!");
    }

    /**
     * 线程安全获取钓鱼状态
     *
     * @return true 在钓鱼
     */
    public synchronized boolean isStatus() {
        if (status) {
            return true;
        } else {
            status = true;
            save();
            return false;
        }
    }

    /**
     * 获取钓鱼状态
     *
     * @return true 在钓鱼
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * 关闭钓鱼状态
     */
    public void switchStatus() {
        this.status = false;
        save();
    }

}
