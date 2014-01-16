package aco.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import aco.AntColony;

//Implementation similar to the one of James McCaffrey, but using Lists instead of Arrays
//(http://msdn.microsoft.com/de-de/magazine/hh781027.aspx)
public class AntColonyList implements AntColony {

	@Override
	public List<Integer> solve(List<Node> nodes, int iterations, int antCount,
			double alpha, double beta, double rho, double Q) {
		List<List<Double>> distances = InitializerList.computeDistances(nodes);
		List<List<Double>> pheromones = InitializerList.initPheromones(nodes);
		List<Ant> ants = InitializerList.initAnts(antCount, nodes);
		List<Integer> bestTrail = getBestTrail(ants, distances);
		double bestLength = getTrailLength(bestTrail, distances);
		printTrail(bestTrail, distances);

		System.out.println(nodes);
		printDistances(distances);
		printPheromones(pheromones);

		int count = 0;
		boolean end = false;
		while (!end) {
			updateTrails(ants, pheromones, distances, alpha, beta);
			updatePheromones(pheromones, ants, distances, rho, Q);

			List<Integer> tmpBestTrail = getBestTrail(ants, distances);
			double tmpBestLength = getTrailLength(tmpBestTrail, distances);
			if (tmpBestLength < bestLength) {
				bestTrail = tmpBestTrail;
				bestLength = tmpBestLength;

				System.out.println(count
						+ " -------------------------------------");
				printTrail(bestTrail, distances);
			}

			if (++count >= iterations) {
				end = true;
			}
		}
		System.out
				.println("\n-------------AntColonyList------------------------");
		printTrail(bestTrail, distances);
		System.out.println("ITERATIONS = " + iterations);
		System.out.println("ANTS = " + antCount);
		System.out.println("ALPHA = " + alpha);
		System.out.println("BETA = " + beta);
		System.out.println("RHO = " + rho);
		System.out.println("Q = " + Q);

		return bestTrail;
	}

	private void updateTrails(List<Ant> ants, List<List<Double>> pheromones,
			List<List<Double>> distances, double alpha, double beta) {
		int antCount = ants.size();
		ants.clear();
		for (int i = 0; i < antCount; i++) {
			Ant ant = createTrail(pheromones, distances, alpha, beta);
			ants.add(ant);
		}
	}

	private Ant createTrail(List<List<Double>> pheromones,
			List<List<Double>> distances, double alpha, double beta) {
		int nodeCount = pheromones.size();
		int start = new Random().nextInt(nodeCount);
		boolean[] visited = new boolean[nodeCount];
		visited[start] = true;
		Ant ant = new Ant(start);
		for (int i = 0; i < nodeCount - 1; i++) {
			int nextNode = getNextNode(ant, visited, pheromones, distances,
					alpha, beta);
			ant.addTrailNode(nextNode);
			visited[nextNode] = true;
		}
		ant.addTrailNode(ant.getStartNodeIndex());
		return ant;
	}

	private int getNextNode(Ant ant, boolean[] visited,
			List<List<Double>> pheromones, List<List<Double>> distances,
			double alpha, double beta) {
		List<Double> moveProbabilities = getMoveProbabilities(ant, visited,
				pheromones, distances, alpha, beta);

		int nodeCount = pheromones.size();
		List<Double> cumul = new ArrayList<Double>();
		cumul.add(0.0);
		for (int i = 0; i < nodeCount; i++) {
			cumul.add(cumul.get(i) + moveProbabilities.get(i));
		}
		cumul.add(1.0);

		double position = new Random().nextDouble();
		for (int i = 0; i < nodeCount; i++) {
			if (position >= cumul.get(i) && position < cumul.get(i + 1)) {
				return i;
			}
		}

		return -1;
	}

	private List<Double> getMoveProbabilities(Ant ant, boolean[] visited,
			List<List<Double>> pheromones, List<List<Double>> distances,
			double alpha, double beta) {
		int nodeCount = pheromones.size();
		List<Double> taueta = new ArrayList<Double>();
		double sum = 0.0;

		for (int i = 0; i < nodeCount; i++) {
			int currentNode = ant.getCurrentNodeIndex();
			double tmp = 0.0;
			if (!visited[i]) {
				tmp = Math.pow(pheromones.get(currentNode).get(i), alpha)
						* Math.pow((1.0 / distances.get(currentNode).get(i)),
								beta);
				if (tmp < 0.0001) {
					tmp = 0.0001;
				} else if (tmp > (Double.MAX_VALUE / (nodeCount * 100))) {
					tmp = Double.MAX_VALUE / (nodeCount * 100);
				}
			}
			sum += tmp;
			taueta.add(tmp);
		}

		List<Double> probabilities = new ArrayList<Double>();
		for (int i = 0; i < nodeCount; i++) {
			probabilities.add(taueta.get(i) / sum);
		}
		return probabilities;
	}

	private List<Integer> getBestTrail(List<Ant> ants,
			List<List<Double>> distances) {
		List<Double> lengths = new ArrayList<Double>();
		for (Ant ant : ants) {
			lengths.add(getTrailLength(ant.getTrail(), distances));
		}
		Double minLength = Collections.min(lengths);
		return ants.get(lengths.indexOf(minLength)).getTrail();
	}

	private double getTrailLength(List<Integer> trail,
			List<List<Double>> distances) {
		double length = 0.0;
		for (int i = 0; i < trail.size() - 1; i++) {
			int start = trail.get(i);
			int end = trail.get(i + 1);
			length += distances.get(start).get(end);
		}
		return length;
	}

	private void updatePheromones(List<List<Double>> pheromones,
			List<Ant> ants, List<List<Double>> distances, double rho, double Q) {
		for (int i = 0; i < pheromones.size(); ++i) {
			for (int j = i + 1; j < pheromones.size(); ++j) {
				for (Ant ant : ants) {
					double length = getTrailLength(ant.getTrail(), distances);
					double decrease = (1.0 - rho) * pheromones.get(i).get(j);
					double increase = 0.0;
					if (ant.containsTrail(i, j)) {
						increase = (Q / length);
					}
					double pheromone = decrease + increase;
					pheromones.get(i).set(j, pheromone);
					pheromones.get(j).set(i, pheromone);
				}
			}
		}
	}

	private void printDistances(List<List<Double>> distances) {
		System.out.println("----Distances----");
		for (List<Double> p : distances) {
			System.out.println(p);
		}
		System.out.println("--------");
	}

	private void printPheromones(List<List<Double>> pheromones) {
		System.out.println("----Pheromones----");
		for (List<Double> p : pheromones) {
			System.out.println(p);
		}
		System.out.println("--------");
	}

	private void printTrail(List<Integer> trail, List<List<Double>> distances) {
		System.out.println(getTrailLength(trail, distances) + ": " + trail);
	}

	private Ant optimalTrail() {
		Ant ant = new Ant(0);
		ant.addTrailNode(7);
		ant.addTrailNode(37);
		ant.addTrailNode(30);
		ant.addTrailNode(43);
		ant.addTrailNode(17);
		ant.addTrailNode(6);
		ant.addTrailNode(27);
		ant.addTrailNode(5);
		ant.addTrailNode(36);
		ant.addTrailNode(18);
		ant.addTrailNode(26);
		ant.addTrailNode(16);
		ant.addTrailNode(42);
		ant.addTrailNode(29);
		ant.addTrailNode(35);
		ant.addTrailNode(45);
		ant.addTrailNode(32);
		ant.addTrailNode(19);
		ant.addTrailNode(46);
		ant.addTrailNode(20);
		ant.addTrailNode(31);
		ant.addTrailNode(38);
		ant.addTrailNode(47);
		ant.addTrailNode(4);
		ant.addTrailNode(41);
		ant.addTrailNode(23);
		ant.addTrailNode(9);
		ant.addTrailNode(44);
		ant.addTrailNode(34);
		ant.addTrailNode(3);
		ant.addTrailNode(25);
		ant.addTrailNode(1);
		ant.addTrailNode(28);
		ant.addTrailNode(33);
		ant.addTrailNode(40);
		ant.addTrailNode(15);
		ant.addTrailNode(21);
		ant.addTrailNode(2);
		ant.addTrailNode(22);
		ant.addTrailNode(13);
		ant.addTrailNode(24);
		ant.addTrailNode(12);
		ant.addTrailNode(10);
		ant.addTrailNode(11);
		ant.addTrailNode(14);
		ant.addTrailNode(39);
		ant.addTrailNode(8);
		ant.addTrailNode(0);
		return ant;
	}
}