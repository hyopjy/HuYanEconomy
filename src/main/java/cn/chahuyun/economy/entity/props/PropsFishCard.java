package cn.chahuyun.economy.entity.props;

import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity(name = "PropsFishCard")
@Table
@Getter
@Setter
public class PropsFishCard extends PropsBase implements Serializable {

    @Column(columnDefinition = "text")
    private String fishDesc;

    @Column(columnDefinition = "text")
    private String content;

    private Boolean buy;

    @Column(columnDefinition = "text")
    private String priceDesc;

    private Boolean exchange;

    public PropsFishCard() {
    }

    public PropsFishCard(String code, String name, int cost, String description, String fishDesc, String content, Boolean buy,String priceDesc,Boolean exchange){
        super(code, name, cost, false, "", description, false, null, null);
        this.buy = buy;
        this.content = content;
        this.fishDesc = fishDesc;
        this.priceDesc = priceDesc;
        this.exchange = exchange;

    }

    @Override
    public String toString() {
        return "道具名称:" + this.getName() +
                "\r\n价格:" + this.getCost() + "币币" +
                "\r\n描述:" + this.getDescription();
    }

    /**
     * 保存
     */
    public PropsFishCard save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

}
