package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Merkle tree used to check whether a transaction is part of a block by hashing
 * it along its merkle branch and checking if it alters the Merkle root.
 */
public class MerkleTree implements JSONable {
    
    private HashMap<Transaction, Node> leaves;
    private Node root;
    
    private static final Integer NO_SIDE = 0; // Merkle root
    private static final Integer LEFT = 1; // Left node of a pair
    private static final Integer RIGHT = 2; // Right node of a pair
    private static final Integer NO_CHILD = -1;
    
    public MerkleTree(final List<Transaction> transactions) {
        root = computeMerkleRoot(transactions);
    }
    
    /**
     * Create MerkleTree instance from a JSON representation
     *
     * @param json
     */
    public MerkleTree(final JSONObject json) {
        final JSONArray jArray = json.getJSONArray("nodes");
        final JSONArray nodeIds = json.getJSONArray("nodeIds");
        final List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < jArray.length(); i++) {
            final Object type = jArray.get(i);
            if (type instanceof JSONObject) {
                nodes.add(new Node((JSONObject) type));
            } else {
                throw new IllegalArgumentException("Unknow object in nodeArray ! " + type);
            }
        }
        // Connect nodes (parents and children were assigned to null because
        // json cannot handle objects with recursive definition
        for (int i = 0; i < nodeIds.length(); i += 2) {
            final Integer nodeId = i / 2;
            if ((Integer) nodeIds.get(i) != NO_CHILD) {
                nodes.get((Integer) nodeIds.getInt(i)).setParent(nodes.get(nodeId));
            }
            if ((Integer) nodeIds.get(i + 1) != NO_CHILD) {
                nodes.get((Integer) nodeIds.getInt(i + 1)).setParent(nodes.get(nodeId));
            }
        }
        root = nodes.get(0);
    }
    
    /**
     * Performs one step of the Merkle root computation by concatenating two
     * nodes and hashing the resulting byte array.
     * 
     * @param leftNode  Left node
     * @param rightNode Right node
     * @return Node representing the hashed concatenation of its children
     */
    public Node concatenateAndHash(final Node leftNode, final Node rightNode) {
        leftNode.setSide(LEFT);
        final byte[] leftHash = leftNode.getHash();
        final byte[] rightHash = rightNode.getHash();
        final ByteBuffer buffer = ByteBuffer.allocate(leftHash.length + rightHash.length);
        buffer.put(leftHash);
        buffer.put(rightHash);
        final byte[] newHash = Cryptography.hashBytes(buffer.array());
        final Node parent = new Node(newHash);
        leftNode.setParent(parent);
        rightNode.setParent(parent);
        return parent;
    }
    
    /**
     * Extracts byte representation of each transaction, then hashes each
     * of them once, and finally pass them to the main hashing algorithm
     * to get the Merkle root.
     * 
     * @param transactions  Transactions from a same block
     * @return  Mekle root
     */
    public Node computeMerkleRoot(final List<Transaction> transactions) {
        this.leaves = new HashMap<>();
        final List<Node> leaves = new ArrayList<>();
        for (final Transaction transaction: transactions) {
            final byte[] hash = Cryptography.hashBytes(transaction.toBytes());
            final Node node = new Node(hash);
            leaves.add(node);
            this.leaves.put(transaction, node);
        }
        return computeMerkleRootFromBytes(leaves);
    }
    
    /**
     * Computes Merkle root by recursively concatenating pairs of hashes and
     * computing their hash. If at any iteration, there is an odd number of
     * hashes, the last hash one is concatenated with itself before to be
     * hashed again.
     * 
     * @param nodes  List of nodes from previous tree level
     * @return   Merkle root
     */
    public Node computeMerkleRootFromBytes(final List<Node> nodes) {
        if (nodes.size() == 1) {
            return nodes.get(0); // The only remaining node is the Merkle root
        }
        else {
            final Integer nPairs = (int) Math.floor(nodes.size() / 2.0);
            final List<Node> nextNodes = new ArrayList<>();
            for (int i = 0; i < nPairs * 2; i += 2) {
                // Concatenate pairs of adjacent transactions and hash them
                nextNodes.add(concatenateAndHash(nodes.get(i), nodes.get(i + 1)));
            }
            if (nodes.size() % 2 == 1) {
                // Hash the single hashafter concatenating with itself
                final Node singleNode = nodes.get(nodes.size() - 1);
                final Node nodeCopy = new Node(singleNode);
                nodeCopy.leftChild = null;
                nodeCopy.rightChild = null;
                nextNodes.add(concatenateAndHash(singleNode, nodeCopy));
            }
            return computeMerkleRootFromBytes(nextNodes);
        }
    }
    
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        try {
            final JSONArray jArray = new JSONArray();
            final JSONArray nodeIds = new JSONArray();
            final Stack<Node> stack = new Stack<>();
            stack.add(root);
            Integer nodeCounter = 0;
            while (!(stack.empty())) {
                final Node currentNode = stack.pop();
                jArray.put(currentNode.toJSON());
                if (currentNode.leftChild != null) {
                    stack.push(currentNode.leftChild);
                    nodeIds.put(++nodeCounter);
                } else {
                    nodeIds.put(NO_CHILD);
                }
                if (currentNode.rightChild != null) {
                    stack.push(currentNode.rightChild);
                    nodeIds.put(++nodeCounter);
                } else {
                    nodeIds.put(NO_CHILD);
                }
            }
            json.put("nodes", jArray);
            json.put("nodeIds", nodeIds);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return json;
    }
    
    public byte[] getRoot() {
        return root.getHash();
    }
    
    /**
     * Node that composes a Merkle tree. It consists of a parent and a hash.
     * Each node hash is concatenated with a neighbor and then hashed. So it
     * is either the left part of a pair, the right part of a pair,
     * or the Merkle root itself.
     */
    class Node implements JSONable {
        private Node parent;
        private Node leftChild;
        private Node rightChild;
        private final byte[] hash;
        private Integer side;
        
        public Node(final byte[] hash) {
            this.hash = hash;
            this.side = NO_SIDE;
        }
        
        /**
         * Copy constructor
         * 
         * @param other 
         */
        public Node(final Node other) {
            this.hash = other.hash;
            this.parent = other.parent;
            this.leftChild = other.leftChild;
            this.rightChild = other.rightChild;
            this.side = other.side;
        }
        
       /**
        * Create Node instance from a JSON representation
        *
        * @param json
        */
        public Node(final JSONObject json) {
            this.hash = JsonUtils.decodeBytes(json.getString("hash"));
            this.parent = null;
            this.leftChild = null;
            this.rightChild = null;
            this.side = json.getInt("side");
        }
        
        @Override
        public JSONObject toJSON() {
            final JSONObject json = JSONable.super.toJSON();
            json.put("hash", JsonUtils.encodeBytes(hash));
            json.put("side", side);
            return json;
        }
        
        public void setParent(final Node parent) {
            this.parent = parent;
            if (this.side == LEFT) {
                parent.setLeftChild(this);
            } else if (this.side == RIGHT) {
                parent.setRightChild(this);
            }
        }
        
        public void setLeftChild(final Node child) {
            this.leftChild = child;
        }
        
        public void setRightChild(final Node child) {
            this.rightChild = child;
        }
        
        public void setSide(final Integer side) {
            this.side = side;
        }
        
        public byte[] getHash() {
            return this.hash;
        }
        
        public Integer getSide() {
            return this.side;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Node other = (Node) obj;
            if (!Arrays.equals(this.hash, other.hash)) {
                return false;
            }
            return true;
        }
    }
}
