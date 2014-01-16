package aco;

import java.util.List;

import aco.list.Node;

public interface AntColony {

	public abstract List<Integer> solve(List<Node> nodes, int iterations,
			int antCount, double alpha, double beta, double rho, double Q);

}