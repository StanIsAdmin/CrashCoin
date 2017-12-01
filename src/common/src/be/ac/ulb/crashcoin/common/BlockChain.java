package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stock block
 */
public class BlockChain extends ArrayList<Block> implements JSONable {

    // Used by [Relay Node]
    public BlockChain(final JSONObject json) {
        final JSONArray blockArray = json.getJSONArray("blockArray");
        
        for(int i = 0; i < blockArray.length(); ++i) {
            final Object type = blockArray.get(0);
            if(type instanceof JSONObject) {
                this.add(new Block((JSONObject) type));
            } else {
                throw new IllegalArgumentException("Unknow object in blockArray ! " + type);
            }
        }
        // TODO
    }

    // Used by [Master node]
    public BlockChain() {
        final Block genesis = createGenesisBlock();
        super.add(genesis);
    }
    
    @Override
    public boolean add(final Block block) {
        if(!this.contains(block)) {
            try {
                if(checkValidBlock(block, Parameters.MINING_DIFFICULTY)) {
                    super.add(block);
                    return true;
                } else {
                    // TODO not valide !
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(BlockChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    private byte[] getLastBlockToBytes() throws NoSuchAlgorithmException {
        return Cryptography.hashBytes(get(this.size()-1).headerToBytes());
    }
    
    
    // Must may be move to Block
    // Used by [master node]
    protected boolean checkValidBlock(final Block block, final int difficulty) throws NoSuchAlgorithmException {
        boolean result = block.isHashValid() && 
                difficulty == block.getDifficulty() &&
                // Previous hash block is valid
                Arrays.equals(block.getPreviousBlock(), this.getLastBlockToBytes());
        
        // TODO
        // Vérifier que les transactions ont comme input des transactions déjà validées 
        //    (i.e. existent dans un bloc précédent – ou le bloc courant(?))
        return result;
    }
    
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        
        try {
            final JSONArray jArray = new JSONArray();
            for (final Block block : this) {
                 jArray.put(block.toJSON());
            }
            json.put("blockArray", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return json;
    }

    protected Block createGenesisBlock() {
        Block genesisBlock = new Block(new byte[0], 0);
        PublicKey masterPublicKey = Cryptography.createPublicKeyFromBytes(Parameters.MASTER_WALLET_PUBLIC_KEY);
        Address masterWallet = new Address(masterPublicKey);
        Timestamp genesisTime = new Timestamp(0L);
        Transaction reward = new Transaction(masterWallet, Parameters.MINING_REWARD, genesisTime);
        genesisBlock.add(reward);
        return genesisBlock;
    }
    
}
