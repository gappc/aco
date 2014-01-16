package aco.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InitializerList {

	public static List<List<Double>> computeDistances(List<Node> nodes) {
		List<List<Double>> globalDistances = new ArrayList<List<Double>>();
		for (Node home : nodes) {
			List<Double> distanceToHome = new ArrayList<Double>();
			for (Node node : nodes) {
				double distance = Math.sqrt(Math.pow(home.getX() - node.getX(), 2) + Math.pow(home.getY() - node.getY(), 2));
				distanceToHome.add(distance);
			}
			globalDistances.add(distanceToHome);
		}
		return globalDistances;
	}
	
	public static List<List<Double>> initPheromones(List<Node> nodes) {
		List<List<Double>> globalPheromones = new ArrayList<List<Double>>();
		for (int i = 0; i < nodes.size(); i++) {
			List<Double> phermones = new ArrayList<Double>();
			for (int j = 0; j < nodes.size(); j++) {
				phermones.add(0.01);
			}
			globalPheromones.add(phermones);
		}
		for (int i = 0; i < nodes.size(); i++) {
			globalPheromones.get(i).set(i, 0.0);
		}
		return globalPheromones;
	}

	public static List<Ant> initAnts(int antCount, List<Node> nodes) {
		Random rand = new Random();
		List<Ant> ants = new ArrayList<Ant>();
		for (int i = 0; i < antCount; i++) {
			Ant ant = getRandomTrail(rand.nextInt(nodes.size()), nodes.size());
			ants.add(ant);
		}
		return ants;
	}
	
	private static Ant getRandomTrail(int start, int size) {
		List<Integer> trail = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			trail.add(i);
		}
		Collections.shuffle(trail);
		Collections.swap(trail, 0, trail.indexOf(start));
		trail.add(trail.get(0));

		return new Ant(trail);
	}
}
