package com.gdut.fundraising.blockchain.Service.impl;

import com.gdut.fundraising.blockchain.Service.MerkleTreeService;
import com.gdut.fundraising.blockchain.Sha256Util;
import com.gdut.fundraising.blockchain.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MerkleTreeServiceImpl implements MerkleTreeService {

    /**
     * 获取梅克尔树的根
     * @param ids
     * @return
     */
    private String getMerkleRootByIds(List<String> ids) {
        if (ids.size() == 1) {
            return ids.get(0);
        }
        List<String> oldIdx = new ArrayList<String>();
        String s = null;
        if (ids.size() % 2 == 1) {
            s = ids.get(ids.size() - 1);
            ids.remove(ids.size()-1);
        }
        for (int i = 0; i< ids.size()/2;i+=2){
            oldIdx.add(Sha256Util.doubleSHA256(ids.get(i)+ids.get(i+1)));
        }
        if(s!=null){
            oldIdx.add(s);
        }
        return getMerkleRootByIds(oldIdx);
    }

    /**
     * 获取梅克尔树的根
     * @param txs
     * @return
     */
    @Override
    public String getMerkleRoot(List<Transaction> txs) {
        List<String> list=new ArrayList<>();
        for(Transaction tx:txs){
            list.add(tx.getId());
        }
        return getMerkleRootByIds(list);
    }
}
