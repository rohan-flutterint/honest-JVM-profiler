package com.insightfullogic.honest_profiler.core.aggregation;

import com.insightfullogic.honest_profiler.core.aggregation.result.diff.TreeDiff;
import com.insightfullogic.honest_profiler.core.aggregation.result.straight.Node;
import com.insightfullogic.honest_profiler.core.aggregation.result.straight.Tree;
import com.insightfullogic.honest_profiler.core.profiles.lean.NumericInfo;

public final class ReferenceUtil
{
    /**
     * Set the reference of all {@link Node}s contained in the provided {@link Tree} according to the specified
     * {@link ReferenceMode}. The reference of a {@link Node} is used for calculating the percentage values.
     *
     * Please consult the {@link ReferenceMode} documentation for more information about the meaning of the reference
     * modes.
     *
     * WARNING : the {@link ReferenceMode#THREAD} mode can only be used for {@link Tree}s or {@link TreeDiff}s where the
     * top-level children represent thread-level aggregations.
     *
     * @param tree the {@link Tree} whose references are to be changed
     * @param mode the strategy for setting the references
     */
    public static <K> void switchReference(Tree<K> tree, ReferenceMode mode)
    {
        switch (mode)
        {
            case GLOBAL:
                NumericInfo global = tree.getSource().getGlobalData();
                tree.flatten().forEach(node -> node.setReference(global));
                return;
            case THREAD:
                tree.getData().forEach(rootNode ->
                {
                    // The root nodes are presumed to be thread-level aggregations. If not, the results are unspecified.
                    NumericInfo reference = rootNode.getData();
                    rootNode.getChildren().forEach(
                        child -> child.flatten().forEach(node -> node.setReference(reference)));
                });
                return;
            case PARENT:
                tree.getData().forEach(node -> setReferenceToParent(null, node));
                return;
        }
    }

    /**
     * Set the reference of all {@link Node}s contained in the base and new {@link Tree}s in the provided Diff according
     * to the specified {@link ReferenceMode}. The reference of a {@link Node} is used for calculating the percentage
     * values.
     *
     * @param treeDiff the {@link TreeDiff} whose references are to be changed
     * @param mode the strategy for setting the references
     */
    public static <K> void switchReference(TreeDiff<K> treeDiff, ReferenceMode mode)
    {
        switchReference(treeDiff.getBaseAggregation(), mode);
        switchReference(treeDiff.getNewAggregation(), mode);
    }

    private static <K> void setReferenceToParent(Node<K> parent, Node<K> child)
    {

        if (parent == null)
        {
            child.setReference(child.getData());
        }
        else
        {
            child.setReference(parent.getData());
            child.getChildren().forEach(grandChild -> setReferenceToParent(child, grandChild));
        }
    }

    private ReferenceUtil()
    {
        // Private Constructor for utility class
    }
}
