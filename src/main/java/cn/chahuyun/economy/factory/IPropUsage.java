package cn.chahuyun.economy.factory;


/**
 * 抽象产品
 */
public interface IPropUsage {

    /**
     * 校验命令
     *
     * @return
     */
    boolean checkOrder();

    void excute();

    boolean checkBuff();

}
