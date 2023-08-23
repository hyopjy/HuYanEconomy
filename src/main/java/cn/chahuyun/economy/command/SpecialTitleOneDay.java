package cn.chahuyun.economy.command;
import cn.chahuyun.economy.factory.AbstractPropUsage;

public class SpecialTitleOneDay extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        return false;
    }

    @Override
    public void excute() {
       //  SpecialTitleManager.init

        // 加入instance 存储

        // 删除定时任务
        // 增加定时任务
    }
}
