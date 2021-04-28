package com.gdut.fundraising.entities;


import java.util.HashMap;
import java.util.Map;

public class GraphPointerEntity {
    String source;
    String target;

    public GraphPointerEntity(){

    }

    public GraphPointerEntity(GraphNodeEntity from,GraphNodeEntity to){
        source=from.getId();
        target=to.getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * (source== null ? 1 : source.hashCode());
        return result+target.hashCode();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GraphPointerEntity){
            GraphPointerEntity graphPointerEntity= (GraphPointerEntity) obj;
            return graphPointerEntity.getSource().equals(source) && graphPointerEntity.getTarget().equals(target);
        }

        return false;
    }

//    public static void main(String[] args) {
//        Map<GraphPointerEntity,Integer> map=new HashMap<>();
//        GraphPointerEntity graphPointerEntity1=new GraphPointerEntity();
//        graphPointerEntity1.setSource("123");
//        graphPointerEntity1.setTarget("456");
//        map.put(graphPointerEntity1,1);
//        GraphPointerEntity graphPointerEntity2=new GraphPointerEntity();
//        graphPointerEntity2.setSource("123");
//        graphPointerEntity2.setTarget("456");
//        System.out.println(graphPointerEntity1.getSource().hashCode());
//        System.out.println(graphPointerEntity2.getSource().hashCode());
//
//        System.out.println(graphPointerEntity1.getSource().equals(graphPointerEntity2.getSource()));
//
//        System.out.println(graphPointerEntity1.hashCode());
//        System.out.println(graphPointerEntity2.hashCode());
//        System.out.println(map.get(graphPointerEntity2));
//        System.out.println(map.get(graphPointerEntity1));
//    }
}
