package cn.chahuyun.economy.utils;

import cn.chahuyun.config.CarDetail;
import cn.chahuyun.config.DriverCarEventConfig;
import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.entity.UserInfo;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.action.UserNudge;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandOperator {
    /**
     * 开车
     *
     * @param message
     * @param group
     * @param senderId
     * @return
     */
    public Message handleToCar(String message, Group group, long senderId) {
        long groupId = group.getId();
        // 开车 独一无二的名字 整数 文案 天选模式
        List<CarDetail> carDetailList = Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>());
        if (CollectionUtil.isEmpty(carDetailList)) {
            carDetailList = new ArrayList<>();
        }
        String[] messages = message.split(" ");
        if (!(messages.length == 4 || messages.length == 5)) {
            return new PlainText("请按照标准格式输入,有两种指令格式开车:").plus("\r\n")
                    .plus("1. 开车 车名 上车人数 文案 天选模式").plus("\r\n")
                    .plus("2. 开车 车名 上车人数 文案").plus("\r\n");
        }
        String carName = messages[1];
        List<String> name = carDetailList.stream().map(CarDetail::getCarName).collect(Collectors.toList());
        if (name.contains(carName)) {
            return new PlainText("车名已存在～");
        }
        int carNumber = 0;
        try {
            carNumber = Integer.parseInt(messages[2]);
            if (carNumber < 1) {
                return new PlainText("上车人数请大于0");
            }
            if (carNumber > 50) {
                return new PlainText("上车人数请不能超过50");
            }
        } catch (NumberFormatException exception) {
            return new PlainText("上车人数请填写整数");
        }
        String carDesc = messages[3];
        String random = "";
        if (messages.length == 5) {
            String randomStr = messages[4];
            if ("天选模式".equals(randomStr)) {
                random = "random";
                carDesc = carDesc + " \uD83C\uDF40";
            }
        }
        List<Long> carUser = new ArrayList<>();
        carUser.add(senderId);
        CarDetail carDetail = new CarDetail(carName, carNumber, carDesc, random, carUser, senderId);
        carDetailList.add(carDetail);

        Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>()).clear();
        DriverCarEventConfig.INSTANCE.getDriverCar().put(groupId,carDetailList);
        // 车满了 就散车m
        return getCarMessage(groupId, carName, "封车", group);
    }

    private synchronized Message getCarMessage(long groupId, String carName, String messageType,Group group) {
        List<CarDetail> carDetailList = Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>());
        Optional<CarDetail> carDetail = carDetailList.stream().filter(car -> car.getCarName().equals(carName)).findFirst();
        double userMoney = 4.00;
        double driverMoney = 10.00;
        double luckMoney = 0.00;
        if (carDetail.isPresent()) {
            CarDetail car = carDetail.get();
            Message message = new PlainText("🚗 ").plus(new At(car.getDriverUser())).plus(" ").plus("开车").plus(" ").plus(car.getCarName() + " ").plus(car.getCarNumber() + "位车友 ").plus(car.getCarDesc()).plus("\r\n");
            int carSize = car.getCarUser().size();
            List<Long> carUsers = car.getCarUser();
            for (int i = 0; i < car.getCarNumber(); i++) {
                if (i < carSize) {
                    if (carUsers.get(i) == car.getDriverUser()) {
                        message = message.plus((i + 1) + ". ").plus(new At(carUsers.get(i))).plus("\r\n");
                    } else {
                        message = message.plus((i + 1) + ". ").plus(new At(carUsers.get(i))).plus("\r\n");
                    }

                } else {
                    //  message = message.plus((i + 1) + ". ").plus("\r\n");
                }
            }
            Long luckBoy = null;

            if (carSize == car.getCarNumber() && messageType.equals("封车")) {
                if ("random".equals(car.getRandom())) {
                    int i = RandomUtil.randomInt(0, car.getCarNumber() - 1);
                    luckBoy = carUsers.get(i);
                    luckMoney = i + 1;
                    message = message.plus("天选之人：").plus(new At(luckBoy)).plus("\r\n");
                }
                Boolean money = setMoney(carUsers, car.getDriverUser(), luckBoy, userMoney, driverMoney, luckMoney, group);
                message = message.plus(car.getCarName() + " 封车！");

                // 删除车
                carDetailList.removeIf(x -> x.getCarName().equals(car.getCarName()));

                Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>()).clear();
                DriverCarEventConfig.INSTANCE.getDriverCar().put(groupId,carDetailList);
            }
            if (messageType.equals("散车")) {
                carDetailList.removeIf(x -> x.getCarName().equals(car.getCarName()));
                Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>()).clear();
                DriverCarEventConfig.INSTANCE.getDriverCar().put(groupId,carDetailList);
                message = message.plus(car.getCarName() + " 已散车！");
            }
            return message;
        } else {
            return new PlainText("车车信息没找到～");
        }
    }

    private Boolean setMoney(List<Long> carUsers, long driverUser, Long luckBoy, double userMoney, double driverMoney, double luckMoney, Group group) {
        if (!EconomyEventConfig.INSTANCE.getEconomyCheckGroup().contains(group.getId())) {
            return false;
        }
        for (int i = 0; i < carUsers.size(); i++) {
            NormalMember member = group.get(carUsers.get(i));
            if (driverUser == carUsers.get(i)) {
                EconomyUtil.plusMoneyToUser(member, driverMoney);
            } else {
                EconomyUtil.plusMoneyToUser(member, userMoney);
            }
        }
        if (Objects.nonNull(luckBoy)) {
            NormalMember member = group.get(luckBoy);
            EconomyUtil.plusMoneyToUser(member, luckMoney);
        }
        return true;
    }


    /**
     * 上车
     *
     * @param message
     * @param group
     * @return
     */
    public Message handleToOnCar(String message, Group group, long senderId) {
        // 上车 [独一无二的名字]
        long groupId = group.getId();
        List<CarDetail> carDetailList =  Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>());
        if (CollectionUtil.isEmpty(carDetailList)) {
            carDetailList = new ArrayList<>();
        }
        String[] messages = message.split(" ");
        if (!(messages.length == 2 || messages.length == 3)) {
            return new PlainText("请按照标准格式输入,上车命令:").plus("\r\n")
                    .plus("1.上车 车名").plus("\r\n")
                    .plus("2.上车 车名 2以上座位数").plus("\r\n")
                    ;
        }
        String carName = messages[1];

        List<String> name = carDetailList.stream().map(CarDetail::getCarName).collect(Collectors.toList());
        if (!name.contains(carName)) {
            return new PlainText("当前车名不存在");
        }
        Optional<CarDetail> carDetailOpt = carDetailList.stream().filter(car -> car.getCarName().equals(carName)).findFirst();
        if (carDetailOpt.isPresent()) {
            CarDetail c = carDetailOpt.get();
            if (c.getCarNumber() == c.getCarUser().size()) {
                return new PlainText("车已满员");
            }
        } else {
            return new PlainText("车车不存在");
        }
        int carCount = 1;
        if(messages.length == 3){
            try {
                carCount = Integer.parseInt(messages[2]);
            }catch (NumberFormatException exceptione){
                return new PlainText("请输入整数座位数");
            }
        }

        List<CarDetail> updateCarDetail = new ArrayList<>();
        for (int i = 0; i < carDetailList.size(); i++) {
            CarDetail carDetail = carDetailList.get(i);
            List<Long> carUser = carDetail.getCarUser();
            if (CollectionUtil.isEmpty(carUser)) {
                carUser = new ArrayList<>();
            }
            if(carName.equals(carDetail.getCarName())){
                // 占两个
                if (carCount == 1) {
                    carUser.add(senderId);
                } else {
                    int number = carDetail.getCarNumber();
                    for (int j = 0 ;j < carCount; j++){
                        if (carUser.size() == number) {
                            break;
                        }
                        carUser.add(senderId);
                    }
                }

            }
            CarDetail up = new CarDetail(carDetail.getCarName(), carDetail.getCarNumber(), carDetail.getCarDesc(), carDetail.getRandom(), carUser, carDetail.getDriverUser());
            updateCarDetail.add(up);
        }

        Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>()).clear();
        DriverCarEventConfig.INSTANCE.getDriverCar().put(groupId,updateCarDetail);

        return getCarMessage(groupId, carName, "封车", group);
    }

    /**
     * 散车
     *
     * @param message
     * @param group
     * @return
     */
    public Message handleToRemoveCar(String message, Group group, long senderId) {
        // 散车
        long groupId = group.getId();
        List<CarDetail> carDetailList =  Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>());
        if (CollectionUtil.isEmpty(carDetailList)) {
            carDetailList = new ArrayList<>();
        }
        String[] messages = message.split(" ");
        if (!(messages.length == 2)) {
            return new PlainText("请按照标准格式输入,散车命令:").plus("\r\n").plus("1.散车 车名").plus("\r\n");
        }
        String carName = messages[1];
        List<String> name = carDetailList.stream().map(CarDetail::getCarName).collect(Collectors.toList());
        if (!name.contains(carName)) {
            return new PlainText("车名不存在");
        }
        Optional<CarDetail> carDetailOpt = carDetailList.stream().filter(car -> car.getCarName().equals(carName)).findFirst();
        if (carDetailOpt.isPresent()) {
            CarDetail c = carDetailOpt.get();
            if (!(c.getDriverUser() == senderId)) {
                return new PlainText("只有车主有权限删除");
            }
        } else {
            return new PlainText("车车不存在");
        }
        // 删除车
        return getCarMessage(groupId, carName, "散车",group);
    }

    public Message handleQueryCar(String message, Group group, long senderId) {
        long groupId = group.getId();
        List<CarDetail> carDetailList = Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(groupId)).orElse(new ArrayList<>());
        Message m =  new At(senderId).plus("当前车列表信息：").plus("\r\n");
        for(int i = 0; i< carDetailList.size();i++){
            CarDetail carDetail = carDetailList.get(i);
            List<Long> carUser = carDetail.getCarUser();
            if(CollectionUtil.isEmpty(carUser)){
                carUser = new ArrayList<>();
            }
            m =  m.plus((i+1)+"." + carDetail.getCarName() + " ("+carUser.size() +"/" + carDetail.getCarNumber() + ")").plus("\r\n");;
        }
        return m;
    }

    public Message handleGetHelp(String message, Group group, long senderId) {
        return new PlainText("\uD83D\uDC81\uD83C\uDFFF").plus(new At(senderId)).plus("开车支持以下指令：").plus("\r\n")
                .plus("🚗 想要开车？").plus("\r\n")
                .plus("输入：开车 车名 上车人数 文案 天选模式").plus("\r\n")
                .plus("输入: 开车 车名 上车人数 文案").plus("\r\n")
                .plus("🚌 想要上车？").plus("\r\n")
                .plus("输入: 上车 车名").plus("\r\n")
                .plus("输入: 上车 车名 2以上座位数").plus("\r\n")
                .plus("\uD83D\uDEE4️ 想要插看当前开车列表？").plus("\r\n")
                .plus("输入: 查询开车列表").plus("\r\n")
                .plus("ps: 间隔一个空格，内容暂不支持带空格字符的" + "\uD83D\uDE09");
    }
}
