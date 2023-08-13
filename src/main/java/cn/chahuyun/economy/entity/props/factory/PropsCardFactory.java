package cn.chahuyun.economy.entity.props.factory;

import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.PropsCard;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.HibernateUtil;

import java.util.Date;

/**
 * @author Erzbir
 * @Date: 2022/11/27 14:15
 */
public class PropsCardFactory implements PropsFactory {
    public static PropsCardFactory INSTANCE = new PropsCardFactory();

    private PropsCardFactory() {

    }

    /**
     * 通过 [code] 取出对应的道具<p>
     * 如果对应类型不对则返回 [null]<p>
     *
     * @param code 道具code
     * @return
     */
    @Override
    public PropsCard create(String code) {
        PropsCard card = null;
        try {
            card = (PropsCard) PropsType.getPropsInfo(code);
        } catch (Exception e) {
            return null;
        }
        card.setGetTime(new Date());
        PropsCard finalCard = card;
        //添加道具到数据库
        card = HibernateUtil.factory.fromTransaction(session -> session.merge(finalCard));
        return card;
    }

    @Override
    public PropsCard create() {
        return new PropsCard();
    }

    @Override
    public PropsBase getPropsBase(String code) {
        PropsBase card;
        try {
            card = PropsType.getPropsInfo(code);
        } catch (Exception e) {
            return null;
        }
        return card;
    }
}
