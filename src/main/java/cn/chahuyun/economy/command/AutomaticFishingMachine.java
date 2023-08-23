package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;

/**
 * 岛岛全自动钓鱼机
 */
public class AutomaticFishingMachine extends AbstractPropUsage {
    /**
     * 创建配置文件
     * 启动定时任务
     * init
     * @return
     */
    @Override
    public boolean checkOrder() {
        return false;
    }

    @Override
    public void excute() {
        // add auto machine cache
        //  open time
        //  cron
        //  endtime
        //  List<fish>
        //  sendmessage
    }
}
