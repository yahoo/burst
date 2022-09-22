import React from "react";
import ReactTreeView from "react-treeview";
import cx from 'classnames';
import {useDispatch, useSelector} from "react-redux";
import {FaIcon} from "../../utility/fa-icon";
import {actions as catalog} from '../../store/reducers/catalog';
import {actions as crosscutting} from '../../store/reducers/crosscutting';

const TreeNode = ({type, object, selected = false, onClick = () => null}) => (
    <span className={cx('tree-node', type, {selected})} onClick={onClick}>
        <FaIcon icon={type === 'domain' ? 'address-card-o' : 'object-group'}/>
        <span>{object.moniker}</span>
    </span>
);

const TreeView = () => {
    const dispatch = useDispatch()
    const {tree, expanded, selected} = useSelector(({catalog, crosscutting}) => ({
        tree: catalog.tree,
        expanded: catalog.expanded,
        selected: crosscutting.selectedDataset,
    }))

    if (!tree) {
        return (
            <div id="catalog-tree" className="burst-border">
                <div className="burst-empty-message"> ready to search...</div>
            </div>
        );
    } else if (tree && tree.length === 0) {
        return (
            <div id="catalog-tree" className="burst-border">
                <div className="burst-empty-message"> no search results...</div>
            </div>
        );
    }

    const isDomainSelected = !selected.view

    return (
        <div id="catalog-tree" className="burst-border">
            {tree.map((domain, i) => (
                <ReactTreeView key={i}
                               nodeLabel={
                                   <TreeNode type="domain" object={domain}
                                             selected={isDomainSelected && selected.domain === domain.pk}
                                             onClick={() => dispatch(crosscutting.selectDataset({domain}))}/>
                               }
                               collapsed={!expanded.includes(domain.pk)}
                               onClick={() => dispatch(catalog.toggleDomain({domain}))}>
                    {domain.children.map((view, j) => (
                        <div key={j}>
                            <TreeNode key={j} type="view" object={view}
                                      selected={selected.view === view.pk}
                                      onClick={() => dispatch(crosscutting.selectDataset({domain, view}))}/>
                        </div>
                    ))}
                    <div>
                            <span className="tree-node view" onClick={() => dispatch(catalog.createView({domainPk: domain.pk}))}>
                                <FaIcon icon="plus" className="text-success"/>
                                <span>Add View</span>
                            </span>
                    </div>
                </ReactTreeView>
            ))}
            <div>
                    <span className="tree-node view" onClick={() => dispatch(catalog.createDomain())}>
                        <FaIcon icon="plus" className="text-success"/>
                        <span>Add Domain</span>
                    </span>
            </div>
        </div>
    );
}

export default TreeView
