package cn.chahuyun.economy.entity.bank;

/**
 * @author Erzbir
 * @Date: 2022/11/29 21:52
 * 取款功能
 */
public class Withdraw extends BankAction {
    public Withdraw(Long id) {
        this.id = id;
    }

    @Override
    public Boolean execute() {
        return null;
    }
}