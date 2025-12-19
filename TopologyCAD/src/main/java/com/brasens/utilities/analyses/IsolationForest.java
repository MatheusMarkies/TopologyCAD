package com.brasens.utilities.analyses;

import com.brasens.utilities.math.Vector2D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IsolationForest {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public class iTree{
        private List<Double> objects = new ArrayList<>();
        private Vector2D bounds = new Vector2D(0, 0);
    }

    List<iTree> isolationTrees = new ArrayList<>();

    public List<Double> calculate(List<Double> dataList){

        List<Double> anomalies = new ArrayList<>();

        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for(Double d : dataList){
            if(minY > d)
                minY = d;
            if(maxY < d)
                maxY = d;
        }

        isolationTrees.add(new iTree(dataList, new Vector2D(minY, maxY)));

        int selectedITree = 0;
        while (true){
            iTree nextITree = new iTree();
            Vector2D newBounds = Vector2D.divide(isolationTrees.get(selectedITree).bounds, new Vector2D(2,2));

            int count = 0;

            for(Double d : isolationTrees.get(selectedITree).getObjects()){
                if(d > newBounds.x() && d < newBounds.y()) {
                    nextITree.getObjects().add(d);
                    count++;
                }
            }

            double newBoundsMinY = Double.MAX_VALUE;
            double newBoundsMaxY = Double.MIN_VALUE;

            for(Double d : nextITree.getObjects()){
                if(newBoundsMinY > d)
                    newBoundsMinY = d;
                if(newBoundsMaxY < d)
                    newBoundsMaxY = d;
            }

            newBounds = new Vector2D(newBoundsMinY, newBoundsMaxY);
            nextITree.setBounds(newBounds);

            isolationTrees.add(nextITree);
            selectedITree ++;

            if(selectedITree > 40)
                break;
        }

        for(Double d : dataList) {
            int iTreesCount = 0;
            for (iTree i : isolationTrees) {
                for(Double f : i.getObjects()){
                    if(f == d)
                        iTreesCount++;
                }
            }
            if(iTreesCount <= 1)
                anomalies.add(d);
        }

        return anomalies;
    }

}
