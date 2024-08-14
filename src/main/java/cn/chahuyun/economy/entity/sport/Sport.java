//package cn.chahuyun.economy.entity.sport;
//
//import cn.chahuyun.economy.utils.HibernateUtil;
//import cn.chahuyun.economy.utils.Log;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.io.Serializable;
//
///**
// * 决斗
// */
//@Entity(name = "Sport")
//@Table
//@Getter
//@Setter
//public class Sport implements Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    // 配置日期
//    private String date;
//
//    // 时间段
//    private String startTime;
//
//    // 时间段
//    private String endTime;
//
//    // 选手
//    private String players;
//
//    // 局数
//    private String number;
//
//    // 玩法（决斗或轮盘）
//    private String playingMethod;
//
//
//    public Sport() {
//    }
//
//    public Sport(String date, String startTime, String endTime, String players, String number, String playingMethod) {
//        this.date = date;
//        this.startTime = startTime;
//        this.endTime = endTime;
//        this.players = players;
//        this.number = number;
//        this.playingMethod = playingMethod;
//    }
//
//    public boolean saveOrUpdate() {
//        try {
//            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
//        } catch (Exception e) {
//            Log.error("神秘商人:更新", e);
//            return false;
//        }
//        return true;
//    }
//
//    public void remove() {
//        HibernateUtil.factory.fromTransaction(session -> {
//            session.remove(this);
//            return null;
//        });
//    }
//}
