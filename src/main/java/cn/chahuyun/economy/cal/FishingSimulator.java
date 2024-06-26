package cn.chahuyun.economy.cal;

import java.util.Random;

public class FishingSimulator {
	// 鱼的种类及其捕获概率
	private static final double[] FISH_PROBABILITIES = {0.1, 0.3, 0.5};
	private static final String[] FISH_NAMES = {"鱼种 1", "鱼种 2", "鱼种 3"};

	// 模拟捕鱼过程
	public static void main(String[] args) {
		int totalSimulations = 100;
		int[] results = new int[FISH_PROBABILITIES.length];

		// 使用Random类生成随机数
		Random random = new Random();

		// 进行模拟
		for (int i = 0; i < totalSimulations; i++) {
			String fish = catchFish(random.nextDouble()); // 生成0到1之间的随机数
			results[getIndexForFish(fish)]++; // 统计捕获结果
		}

		// 输出模拟结果
		System.out.println("总共进行了 " + totalSimulations + " 次模拟捕鱼");

		for (int i = 0; i < FISH_PROBABILITIES.length; i++) {
			System.out.println(FISH_NAMES[i] + ": " + results[i] + " 次");
		}
	}

	// 根据随机数决定捕获哪种鱼
	private static String catchFish(double randomValue) {
		if (randomValue < FISH_PROBABILITIES[0]) {
			return FISH_NAMES[0];
		} else if (randomValue < FISH_PROBABILITIES[0] + FISH_PROBABILITIES[1]) {
			return FISH_NAMES[1];
		} else {
			return FISH_NAMES[2];
		}
	}

	// 获取鱼的索引
	private static int getIndexForFish(String fishName) {
		for (int i = 0; i < FISH_NAMES.length; i++) {
			if (FISH_NAMES[i].equals(fishName)) {
				return i;
			}
		}
		return -1; // 如果未找到匹配的鱼种，返回-1
	}
}


// 164
// 0.9322%	0.8939%
