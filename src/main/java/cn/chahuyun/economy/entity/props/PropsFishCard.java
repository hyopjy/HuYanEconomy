package cn.chahuyun.economy.entity.props;

import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Optional;

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

    // 是否兑换
    private Boolean exchange;
    // 是否直接兑换
    private Boolean delete;

    // 是否可交易
    private Boolean tradable;

    // 是否下架
    private Boolean offShelf;

    public PropsFishCard() {
    }

    public PropsFishCard(String code,
                         String name,
                         int cost,
                         String description,
                         String fishDesc,
                         String content,
                         Boolean buy,
                         String priceDesc,
                         Boolean exchange,
                         Boolean delete,
                         Boolean tradable,
                         Boolean offShelf){
        super(code, name, cost, false, "", description, false, null, null);
        this.buy = buy;
        this.content = content;
        this.fishDesc = fishDesc;
        this.priceDesc = priceDesc;
        this.exchange = exchange;
        this.delete = delete;
        this.tradable = tradable;
        this.offShelf = offShelf;
    }

    public PropsFishCard(Long id,
                         String code,
                         String name,
                         int cost,
                         String description,
                         String fishDesc,
                         String content,
                         Boolean buy,
                         String priceDesc,
                         Boolean exchange,
                         Boolean delete,
                         Boolean tradable,
                         Boolean offShelf){
        super(id, code, name, cost, false, "", description, false, null, null);
        this.buy = buy;
        this.content = content;
        this.fishDesc = fishDesc;
        this.priceDesc = priceDesc;
        this.exchange = exchange;
        this.delete = delete;
        this.tradable = tradable;
        this.offShelf = offShelf;
    }

    @Override
    public String toString() {
        String price = this.getCost() +"币币";
        if(this.getCost() < 0){
            price = this.getPriceDesc();
        }
        return "道具名称:" + this.getName() +
                "\r\n价格:" + price +
                "\r\n描述:" + this.getDescription();
    }

    /**
     * 保存
     */
    public PropsFishCard save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public Boolean getExchange() {
        return Optional.ofNullable(exchange).orElse(false);
    }

    public Boolean getDelete() {
        return Optional.ofNullable(delete).orElse(false);
    }

    public Boolean getTradable() {
        return Optional.ofNullable(tradable).orElse(false);
    }

    public Boolean getOffShelf() {
        return Optional.ofNullable(offShelf).orElse(false);
    }


}
