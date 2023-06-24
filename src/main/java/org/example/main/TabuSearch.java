package org.example.main;

//import org.apache.commons.math3.random.RandomDataGenerator;
import org.example.model.Objeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.example.service.FileReader.getObjetos;
import static org.example.util.Values.LIMITE_MOCHILA;
import static org.example.util.Values.QUANTIDADE_ITENS;


public class TabuSearch {

    private int tabuSize;
    private List<int[]> tabuList;
    private int capacity;

    public TabuSearch(int tabuSize, int capacity) {
        this.tabuSize = tabuSize;
        this.capacity = capacity;
        tabuList = new ArrayList<>();
    }

    public Knapsack search(Knapsack initialSolution, int iterations) {
        Knapsack bestSolution = initialSolution;
        Knapsack nextSolution = initialSolution;

        for (int i = 0; i < iterations; i++) {
            List<Knapsack> neighbors = generateNeighbors(nextSolution);
            nextSolution = null;

            for (Knapsack neighbor : neighbors) {
                if (!isTabu(neighbor) && neighbor.getWeight() <= capacity &&
                        (nextSolution == null || neighbor.getValue() > nextSolution.getValue())) {
                    nextSolution = neighbor;
                }
            }

            if (nextSolution == null) {
                break; // No feasible neighbors found
            }

            tabuList.add(nextSolution.getSelectedItems());

            if (tabuList.size() > tabuSize) {
                tabuList.remove(0);
            }

            if (nextSolution.getValue() > bestSolution.getValue()) {
                bestSolution = nextSolution;
            }

            System.out.println("ITERAÇÃO " + (i + 1) + ", MELHOR VALOR: " + bestSolution.getValue());
        }

        return bestSolution;
    }

    private boolean isTabu(Knapsack knapsack) {
        for (int[] tabuItems : tabuList) {
            if (Arrays.equals(tabuItems, knapsack.getSelectedItems())) {
                return true;
            }
        }
        return false;
    }

    private List<Knapsack> generateNeighbors(Knapsack knapsack) {
        List<Knapsack> neighbors = new ArrayList<>();

        int numItems = knapsack.getNumItems();
        int[] items = knapsack.getSelectedItems();

        for (int i = 0; i < numItems; i++) {
            int[] neighborItems = Arrays.copyOf(items, numItems);
            neighborItems[i] = (neighborItems[i] + 1) % 2;

            neighbors.add(new Knapsack(knapsack.getValues(), knapsack.getWeights(), neighborItems));
        }

        return neighbors;
    }

    // Knapsack class represents a candidate solution
    private class Knapsack {
        private int[] values;
        private int[] weights;
        private int[] selectedItems;

        public Knapsack(int[] values, int[] weights, int[] selectedItems) {
            this.values = values;
            this.weights = weights;
            this.selectedItems = selectedItems;
        }

        public int getValue() {
            int totalValue = 0;
            for (int i = 0; i < values.length; i++) {
                if (selectedItems[i] == 1) {
                    totalValue += values[i];
                }
            }
            return totalValue;
        }

        public int getWeight() {
            int totalWeight = 0;
            for (int i = 0; i < weights.length; i++) {
                if (selectedItems[i] == 1) {
                    totalWeight += weights[i];
                }
            }
            return totalWeight;
        }

        public int[] getValues() {
            return values;
        }

        public int[] getWeights() {
            return weights;
        }

        public int[] getSelectedItems() {
            return selectedItems;
        }

        public int getNumItems() {
            return values.length;
        }
    }

    /** INICIO HIBRIDIZAÇÃO */
    private Knapsack applyEvolutionaryDifferential(Knapsack initialSolution, int iterations) {
        Knapsack bestSolution = initialSolution;
        Knapsack nextSolution = initialSolution;

//        RandomDataGenerator random = new RandomDataGenerator();
        Random random = new Random();


        for (int i = 0; i < iterations; i++) {
            Knapsack mutatedSolution = mutateSolution(nextSolution, random);
            Knapsack trialSolution = recombineSolutions(nextSolution, mutatedSolution, random);

            if (trialSolution.getValue() > nextSolution.getValue() &&
                    trialSolution.getWeight() <= capacity) {
                nextSolution = trialSolution;
            }

            if (nextSolution.getValue() > bestSolution.getValue()) {
                bestSolution = nextSolution;
            }

            System.out.println("ITERAÇÃO " + (i + 1) + ", MELHOR VALOR: " + bestSolution.getValue());
        }

        return bestSolution;
    }

    private Knapsack mutateSolution(Knapsack solution, Random random) {
        int numItems = solution.getNumItems();
        int[] mutatedItems = Arrays.copyOf(solution.getSelectedItems(), numItems);

        // Aplica uma mutação aleatória em um item
        int index = random.nextInt(0, numItems - 1);
        mutatedItems[index] = 1 - mutatedItems[index];

        return new Knapsack(solution.getValues(), solution.getWeights(), mutatedItems);
    }

//    private Knapsack recombineSolutions(Knapsack solution1, Knapsack solution2, RandomDataGenerator random) {
//        int numItems = solution1.getNumItems();
//        int[] recombinedItems = new int[numItems];
//
//        // Combina os itens das duas soluções
//        for (int i = 0; i < numItems; i++) {
//            recombinedItems[i] = random.nextBoolean() ? solution1.getSelectedItems()[i] : solution2.getSelectedItems()[i];
//        }
//
//        return new Knapsack(solution1.getValues(), solution1.getWeights(), recombinedItems);
//    }
        private Knapsack recombineSolutions(Knapsack solution1, Knapsack solution2, Random random) {
            int numItems = solution1.getNumItems();
            int[] recombinedItems = new int[numItems];

            // Combina os itens das duas soluções
            for (int i = 0; i < numItems; i++) {
                recombinedItems[i] = random.nextBoolean() ? solution1.getSelectedItems()[i] : solution2.getSelectedItems()[i];
            }

            return new Knapsack(solution1.getValues(), solution1.getWeights(), recombinedItems);
        }



    public static void main(String[] args) {

        TabuSearch tabuSearch = new TabuSearch(10, LIMITE_MOCHILA.intValue());
        

        int[] pesos = new int[QUANTIDADE_ITENS];
        int[] valores = new int[QUANTIDADE_ITENS];
        int[] solucaoInicial = new int[QUANTIDADE_ITENS]; // Solução inicial


        Knapsack initialSolution = tabuSearch.new Knapsack(valores, pesos, solucaoInicial);

        long init = System.currentTimeMillis();
        List<Objeto> objetos = getObjetos();
        for (int i = 0; i < objetos.size(); i++) {
            pesos[i] = objetos.get(i).getPeso().intValue();
            valores[i] = objetos.get(i).getValorTotal().intValue();
        }
//        Knapsack bestSolution = tabuSearch.search(initialSolution, 100);
        Knapsack bestSolution = tabuSearch.applyEvolutionaryDifferential(initialSolution, 100);

        long finish = System.currentTimeMillis();

        int pesoArmazenado = 0;
        for(int i=0; i<objetos.size(); i++){
            if(bestSolution.getSelectedItems()[i]==1){
                pesoArmazenado += objetos.get(i).getPeso().intValue();
            }
        }
        System.out.println("\nMelhores Itens inclusos: " + Arrays.toString(bestSolution.getSelectedItems()));
        System.out.println("\n");
        System.out.println("Melhor Valor da Solução: " + bestSolution.getValue());
        System.out.println("Peso Minimo Atingido: " + bestSolution.getWeight());
        System.out.println("TEMPO TOTAL DA EXECUÇÃO: " + (finish-init) + " (milisegundos)");

    }
}

