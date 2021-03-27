package com.gdut.fundraising.blockchain.Service.impl;

import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.Service.UTXOService;
import com.gdut.fundraising.blockchain.UTXO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UTXOServiceImpl implements UTXOService {
    /**
     * 返回被删除的utxo
     * @param utxoHashMap
     * @param pointerList
     * @return
     */
    @Override
    public ConcurrentHashMap<Pointer, UTXO> deleteUTXOByPointer(ConcurrentHashMap<Pointer, UTXO> utxoHashMap, List<Pointer> pointerList) {
        ConcurrentHashMap<Pointer, UTXO> deletedUTXO=new ConcurrentHashMap<>();
        for(Pointer p: pointerList){
            if(utxoHashMap.containsKey(p)){
                deletedUTXO.put(p,utxoHashMap.get(p));
                utxoHashMap.remove(p);
            }
        }
        return deletedUTXO;
    }

    /**
     * 添加utxo到utxo map
     * @param utxoHashMap
     * @param utxoList
     */
    @Override
    public void addUTXOToMap(ConcurrentHashMap<Pointer, UTXO> utxoHashMap, List<UTXO> utxoList) {
        for(UTXO utxo:utxoList){
            utxoHashMap.put(utxo.getPointer(),utxo);
        }
    }
}
