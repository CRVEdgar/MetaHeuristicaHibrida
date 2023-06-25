package org.example.main;

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

    public Mochila search(Mochila initialSolution, int iterations) {
        Mochila bestSolution = initialSolution;
        Mochila nextSolution = initialSolution;

        for (int i = 0; i < iterations; i++) {
            List<Mochila> neighbors = gerarVizinhos(nextSolution);
            nextSolution = null;

            for (Mochila neighbor : neighbors) {
                if (!isTabu(neighbor) && neighbor.getWeight() <= capacity &&
                        (nextSolution == null || neighbor.getValue() > nextSolution.getValue())) {
                    nextSolution = neighbor;
                }
            }

            if (nextSolution == null) {
                break; // Nenhum vizinho viável encontrado
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

    private boolean isTabu(Mochila mochila) {
        for (int[] tabuItems : tabuList) {
            if (Arrays.equals(tabuItems, mochila.getSelectedItems())) {
                return true;
            }
        }
        return false;
    }

    private List<Mochila> gerarVizinhos(Mochila mochila) {
        List<Mochila> vizinhos = new ArrayList<>();

        int numItems = mochila.getNumItems();
        int[] items = mochila.getSelectedItems();

        for (int i = 0; i < numItems; i++) {
            int[] neighborItems = Arrays.copyOf(items, numItems);
            neighborItems[i] = (neighborItems[i] + 1) % 2;

            vizinhos.add(new Mochila(mochila.getValues(), mochila.getWeights(), neighborItems));
        }

        return vizinhos;
    }

    private class Mochila {
        private int[] values;
        private int[] pesos;
        private int[] selectedItems;

        public Mochila(int[] values, int[] pesos, int[] selectedItems) {
            this.values = values;
            this.pesos = pesos;
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
            for (int i = 0; i < pesos.length; i++) {
                if (selectedItems[i] == 1) {
                    totalWeight += pesos[i];
                }
            }
            return totalWeight;
        }

        public int[] getValues() {
            return values;
        }

        public int[] getWeights() {
            return pesos;
        }

        public int[] getSelectedItems() {
            return selectedItems;
        }

        public int getNumItems() {
            return values.length;
        }
    }

    /** INICIO HIBRIDIZAÇÃO */
    private Mochila evolucaoDiferencial(Mochila initialSolution, int iterations) {
        Mochila bestSolution = initialSolution;
        Mochila nextSolution = initialSolution;

        Random random = new Random();


        for (int i = 0; i < iterations; i++) {
            Mochila mutatedSolution = solucaoMutante(nextSolution, random);
            Mochila trialSolution = recombinandoSolucao(nextSolution, mutatedSolution, random);

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

    private Mochila solucaoMutante(Mochila solution, Random random) {
        int numItems = solution.getNumItems();
        int[] mutatedItems = Arrays.copyOf(solution.getSelectedItems(), numItems);

        // Aplicando uma mutação aleatória em um item
        int index = random.nextInt(0, numItems - 1);
        mutatedItems[index] = 1 - mutatedItems[index];

        return new Mochila(solution.getValues(), solution.getWeights(), mutatedItems);
    }

        private Mochila recombinandoSolucao(Mochila solution1, Mochila solution2, Random random) {
            int numItems = solution1.getNumItems();
            int[] recombinedItems = new int[numItems];

            // Combina os itens das duas soluções
            for (int i = 0; i < numItems; i++) {
                recombinedItems[i] = random.nextBoolean() ? solution1.getSelectedItems()[i] : solution2.getSelectedItems()[i];
            }

            return new Mochila(solution1.getValues(), solution1.getWeights(), recombinedItems);
        }



    public static void main(String[] args) {

        TabuSearch tabuSearch = new TabuSearch(10, LIMITE_MOCHILA.intValue());

        int[] pesos = new int[QUANTIDADE_ITENS];
        int[] valores = new int[QUANTIDADE_ITENS];
        int[] solucaoInicial = new int[QUANTIDADE_ITENS]; // Solução inicial


        Mochila initialSolution = tabuSearch.new Mochila(valores, pesos, solucaoInicial);

        long init = System.currentTimeMillis();
        List<Objeto> objetos = getObjetos();
        for (int i = 0; i < objetos.size(); i++) {
            pesos[i] = objetos.get(i).getPeso().intValue();
            valores[i] = objetos.get(i).getValorTotal().intValue();
        }
//        Knapsack bestSolution = tabuSearch.search(initialSolution, 100);
        Mochila bestSolution = tabuSearch.evolucaoDiferencial(initialSolution, 100);

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

