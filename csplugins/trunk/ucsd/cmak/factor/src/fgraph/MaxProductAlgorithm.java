package fgraph;

import fgraph.util.IntListMap;
import fgraph.util.IntIntListMap;

import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.list.IntArrayList;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.logging.Logger;
import java.util.logging.Level;


public class MaxProductAlgorithm
{
    private static Logger logger = Logger.getLogger(MaxProductAlgorithm.class.getName());

    private static boolean INFO = logger.isLoggable(Level.INFO);
    private static boolean FINE = logger.isLoggable(Level.FINE);
    private static boolean FINER = logger.isLoggable(Level.FINER);

    
    // filter submodels that do not explain any knockout effects
    private boolean filter = true;
    
    private FactorGraph _fg;

    // A list of Submodel objects generated by MaxProductAndDecompose
    protected List _submodels;
    
    
    public MaxProductAlgorithm(FactorGraph fg)
    {
        _fg = fg;
        _submodels = new ArrayList();
    }
    
    /**
     * Run the max product algorithm.
     *
     * FIX: termination condition.
     */
    public void runMaxProductNonrecursive() throws AlgorithmException
    {
        _runMaxProduct();
        _fg.updateEdgeAnnotation();

        //System.out.println(printAdj());
    }

    public void runMaxProduct() throws AlgorithmException
    {
        Submodel invariant = recursiveMaxProduct();
        
        _submodels.add(0, invariant);
        _submodels = mergeAllSubmodels(_submodels);
        
        // annotate edges of submodel
        _fg.annotateSubmodelEdges(_submodels);

        // send submodels to interaction graph
        _fg.getInteractionGraph().setSubmodels(_submodels);
    }
    
    
    /**
     * Run the max product algorithm and decompose the active network
     * into submodels.
     *
     * FIX: termination condition.
     */
    public void runMaxProductAndDecompose() throws AlgorithmException
    {
        Submodel invariant = recursiveMaxProduct();
        
        logger.info("submodels before merge");
        printSubmodels(_submodels);
        
        // merge submodels
        _submodels = mergeSubmodels(_submodels);

        printSubmodels(_submodels);
        
        // filter submodels that do not explain any knockout effects
        if(filter)
        {
            for(ListIterator it = _submodels.listIterator(); it.hasNext();)
            {
                Submodel m = (Submodel) it.next(); 
                int numKO = countKO(m);
                m.setNumExplainedKO(numKO);
                if( numKO < 1 )
                {
                    logger.info("Submodel " + m.getId() + " explains no KO's");
                                
                    it.remove();
                }
            }
        }

        // make the invariant model the first submodel
        _submodels.add(0, invariant);
                
        // annotate edges of submodel
        _fg.annotateSubmodelEdges(_submodels);

        // print submodels
        logger.info("### Decomposed models: " + _submodels.size());
        
        for(Iterator it = _submodels.iterator(); it.hasNext();)
        {
            logger.info(_fg.toString((Submodel) it.next()));
        }
        
        logger.info("### Found " + _submodels.size() + " submodels");
        
        // send submodels to interaction graph
        _fg.getInteractionGraph().setSubmodels(_submodels);
    }


    
    /**
     * Recursively run the max product algorithm until all model
     * variables have been uniquely deterimed.
     *
     * @return the invariant model.  Other models are added to the
     * _submodels array.
     * @throws AlgorithmException
     */
    private Submodel recursiveMaxProduct() throws AlgorithmException
    {
        _submodels.clear();

        _runMaxProduct();
        Submodel invariant = fixUniqueVars();
        invariant.setInvariant(true);
        invariant.setNumExplainedKO(countKO(invariant));
        
        logger.info("added invariant submodel with "
                    + invariant.size() + " vars");
        
        // annotate invariant edges in the interaction graph
        _fg.updateEdgeAnnotation();
        
        // first fix degenerate sign variables
        int x=0;
        x += recursivelyFixVars(_fg.getSigns());
        
        // now fix any other non-sign variables that are degenerate
        x += recursivelyFixVars(_fg.getDirs());

        logger.info("Called max product method " + x + " times");

        logger.info("### Generated " + _submodels.size()
                    + " submodels + 1 invariant model");
        
        // update the edge annotations now that all variables are fixed.
        _fg.updateEdgeAnnotation();

        return invariant;
    }


    private void _runMaxProduct()  throws AlgorithmException
    {
        _fg.getVars().trimToSize();
        _fg.getFactors().trimToSize();
        
        int[] v = _fg.getVars().elements();
        int[] f = _fg.getFactors().elements();

        initVar2Factor(v);
        
        int N = 2  * _fg.getPaths().getMaxPathLength();
        //int N =  _paths.getMaxPathLength();
        //int N = 20  * _paths.getMaxPathLength();
        //int N = 2;
        
        logger.info("Running max product " + N + " iterations");
        
        for(int x=0; x < N; x++)
        {
            if(x != 0)
            {
                computeVar2Factor(v);
            }
            computeFactor2Var(f);
        }


        // print out messages for debugging
        if(FINE)
        {
            try
            {
                PrintStream pathFactorOut = new PrintStream(new FileOutputStream("pathFactor.msg"));
                pathFactorOut.println(_fg.printAdjOfType(NodeType.PATH_FACTOR));
                
                
                PrintStream orOut = new PrintStream(new FileOutputStream("orFactor.msg"));
                orOut.println(_fg.printAdjOfType(NodeType.OR_FACTOR));
            }
            catch(IOException e)
            {
                logger.info(e.getMessage());
            }
        }
    }



    
    private void printSubmodels(List submodels)
    {
        for(int x=0; x < submodels.size(); x++)
        {
            Submodel s = (Submodel) submodels.get(x);

            IntArrayList vars = s.getVars();
            StringBuffer b = new StringBuffer("model ");
            b.append(s.getId());
            b.append(" [");
            for(int v=0; v < vars.size(); v++)
            {
                b.append(vars.get(v) + " ");
            }
            b.append("]");

            logger.info(b.toString());
        }

    }
    
    /**
     * FIX THIS.  Currently incorrect 10/30/04
     * It returns the number of knocked-out transcription factors
     * that have at least one target in the model.  We want to
     * return the number of knockout effects.
     * 
     * @return the number of knockout effects explained in
     * a submodel
     */
    public int countKO(Submodel m)
    {
        int cnt = 0;
        IntArrayList vars = m.getVars();
        for(int x=0; x < vars.size(); x++)
        {
            VariableNode vn = _fg.getVarNode(vars.get(x));
            if(vn.isType(NodeType.KO))
            {
                cnt++;
            }
        }

        return cnt;
    }


    /**
     * Merge submodels that share one or more edges.
     *
     * @param models a list of Submodel objects
     * @return a list of merged models
     */
    public List mergeSubmodels(List models)
    {
        List merged = new ArrayList();
        
        for(int x=0; x < models.size(); x++)
        {
            boolean isDistinct = true;
            Submodel mX = (Submodel) models.get(x);

            for(int y=0; y < merged.size(); y++)
            {
                Submodel mY = (Submodel) merged.get(y);

                if(mY.overlaps(mX))
                {
                    logger.info("overlap found: m=" + x
                                + ", merged=" + y);
                    mY.merge(mX);
                    isDistinct = false;
                    break;
                }
            }

            if(isDistinct)
            {
                logger.info(mX + " is distinct submodel");
                merged.add(mX);
            }
        }

        return merged;
    }


    /**
     * Merge all submodels into one model
     *
     * @param models a list of Submodel objects
     * @return a list containing the merged model
     */
    public List mergeAllSubmodels(List models)
    {
        List merged = new ArrayList();

        Submodel m0 = (Submodel) models.get(0);
        for(int x=1; x < models.size(); x++)
        {
            m0.merge((Submodel) models.get(x));
        }

        merged.add(m0);
        return merged;
    }

    

    /**
     * Manually fix a var in the input list, re-run the max-product
     * algorithm, fix the vars that are uniquely determined, recurse
     * until all nodes in the input list are fixed.
     *
     * @param varNodes a list of variable node indices
     * @return the number of variables that were manually fixed.
     */
    private int recursivelyFixVars(IntArrayList varNodes)
        throws AlgorithmException
    {
        int x;
        
        // Even though we are not iterating through varNods,
        // this loop is more efficient than using allFixed(varNodes)
        // and it guarantees that we won't get stuck in an infinte
        // loop.
        int N = varNodes.size();
        for(x=0; x < N; x++)
        {
            // Get the node that is connected to the most number
            // of undetermined variables.
            int var = _fg.chooseMaxConnode(varNodes);
            
            if(var < 0)
            {
                _fg.fixVar(var);
            
                _runMaxProduct();

                // find all active paths (ie. sigma vars with max state == 1)
                //int[] activePaths = getActivePaths();
                    
                Submodel model = fixUniqueVars(var);
                
                //model.setActivePaths(activePaths);
                
                _submodels.add(model);
                logger.info("added submodel " + model.getId()
                            + " with " + model.size() + " vars. "
                            + countDegenerate(varNodes) + " vars still not fixed");
            }
            else
            {
                // var == 0 means that all of the nodes are fixed.
                break;
            }
        }
        
        return x;
    }

    /**
     * This is a helper method for @see #recursivelyFixVars
     * 
     * @param nodes a list of variable nodes indicies
     * @return the number of variables that are not fixed.
     */
    private int countDegenerate(IntArrayList nodes)
    {
        int notFixed = 0; 
        for(int y=0; y < nodes.size(); y++)
        {
            int v = nodes.get(y);
            
            if(!_fg.getVarNode(v).isFixed())
            {
                notFixed++;
            }
        }

        return notFixed;
    }

    
    /**
     * Fix all variables that have a unique max
     * @return a Submodel containing the variables that were fixed
     */
    private Submodel fixUniqueVars()
    {
        Submodel model = new Submodel();
        model.setInvariant(true);

        _fixUniqueVars_Helper(model);
        
        return model;
    }

    private Submodel fixUniqueVars(int fixedNode)
    {
        Submodel model = new Submodel();
        model.setIndependentVar(fixedNode);

        IntArrayList depVars = _fixUniqueVars_Helper(model);

        // add variables that influence the
        // potential functions that determine each dependent variable
        for(int x=0; x < depVars.size(); x++)
        {
            int v = depVars.get(x);
            _fg.addInferredVars(model, fixedNode, v);
        }
        
        return model;
    }

    
    /**
     * 1. Fix all variables that have a unique max.
     *
     * 2. Establish dependency relationship from fixedNode to
     * the newly fixed nodes.
     *
     * @param indepVar the independent variable that was manually fixed
     * @param invariant TRUE if the returned model is the invariant model,
     * FALSE otherwise.
     */
    private IntArrayList _fixUniqueVars_Helper(Submodel model)
    {
        int ct = 0;
        IntArrayList depVars = new IntArrayList();

        ct += addUniqueVarsToModel(model, _fg.getEdges(), depVars);
        ct += addUniqueVarsToModel(model, _fg.getDirs(), depVars);
        ct += addUniqueVarsToModel(model, _fg.getSigns(), depVars);

        ct += addUniqueVarsToModel(model, _fg.getKos(), depVars);

        model.setNumDepVars(ct);
        
        return depVars;
    }
    
    private int addUniqueVarsToModel(Submodel model,
                                     IntArrayList vars,
                                     IntArrayList depVars)
    {
        int ct = 0;
        for(int x=0; x < vars.size(); x++)
        {
            int v = vars.get(x);
            VariableNode vn = _fg.getVarNode(v);
            
            ProbTable pt = vn.getProbs();
            
            if(!vn.isFixed() && pt.hasUniqueMax())
            {
                if(vn.isType(NodeType.KO) && _fg.isExplained(v))
                {
                    if(FINE)
                    {
                        logger.fine("  isKO and explained: " + vn);
                    }
                    
                    vn.fixState(pt.maxState());
                    ct++;

                    model.addVar(v);
                    depVars.add(v);
                }
                else if(vn.isType(NodeType.EDGE) ||
                        vn.isType(NodeType.SIGN) ||
                        vn.isType(NodeType.DIR)
                        )
                    // node is an EDGE, SIGN, or DIR variable
                {
                    if(FINE)
                    {
                        logger.fine("fixing " + vn
                                    + " to state: " + pt.maxState());
                    }
                    _fg.fixEdgeSignDir(vn, pt.maxState(), v);
                    depVars.add(v);
                    ct++;

                    if(model.acceptsType(vn.type()))
                    {
                        model.addVar(v);

                    }
                }
            }
            else
            {
                if(FINE)
                {
                    logger.fine("## skipping " + vn + " isFixed=" + vn.isFixed()
                                + " uniqueMax=" + pt.hasUniqueMax());
                }
            }
                
        }

        return ct;
    }


    /**
     * Compute variable to factor messages
     *
     * @param nodes the root graph indicies of variable nodes
     * @return false
     */
    protected boolean computeVar2Factor(int[] nodes)
    {
        boolean noChange = false;
        
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            VariableNode vn = _fg.getVarNode(n);
            List messages = _fg.getAdjacentMessages(n);

            vn.maxProduct(messages, false);

            /* no need since EdgeMessages have a ref to ProbTables?
             * is this unsafe?
            ProbTable pt = vn.getProbs();

            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);
                em.v2f(pt);
            }
            */
        }

        return noChange;
    }

    /**
     * Compute factor to variable messages
     *
     * @param nodes the root graph indicies of factor nodes
     * @throws AlgorithmException
     */
    protected void computeFactor2Var(int[] nodes) throws AlgorithmException
    {
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            FactorNode fn = _fg.getFactorNode(n);

            List messages = _fg.getAdjacentMessages(n);
            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);

                em.f2v(fn.maxProduct(messages, m));
            }
        }
    }

    /**
     * Initialize messages from variable to factor nodes
     *
     * @param nodes root graph indices of variable nodes
     */
    protected void initVar2Factor(int[] nodes)
    {
        for(int x=0, N=nodes.length; x < N; x++)
        {
            int n = nodes[x];
            VariableNode vn = _fg.getVarNode(n);
            ProbTable pt = vn.getProbs();
            
            List messages = _fg.getAdjacentMessages(n);
            for(int m=0, M=messages.size(); m < M; m++)
            {
                EdgeMessage em = (EdgeMessage) messages.get(m);
                em.v2f(pt);
            }
        }
    }
}
