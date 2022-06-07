import React, {useState} from "react";
import {useDispatch} from "react-redux";
import TreeView from "./tree-view";
import CatalogEditor from "./entity-editor";

import './catalog.scss';
import {Button, Form, Navbar} from "react-bootstrap";
import {FaIcon} from "../../utility/fa-icon";
import {actions as catalog} from "../../store/reducers/catalog";
import {Pane} from "../../components/pane";

const SearchBar = () => {
    const dispatch = useDispatch();
    const [domain, setDomain] = useState('');
    const [view, setView] = useState('');

    const doSearch = e => {
        e.preventDefault()
        dispatch(catalog.search({domain, view}))
    }
    return (
        <Navbar className="w-100">
            <Form className="row row-cols-lg-auto align-items-center">
                <label style={{marginRight: '-10px'}}>Domain</label>
                <Form.Group>
                    <Form.Control placeholder="all" value={domain} onChange={e => setDomain(e.target.value)}/>
                </Form.Group>
                <label style={{marginRight: '-10px'}}>View</label>
                <Form.Group>
                    <Form.Control placeholder="all" value={view} onChange={e => setView(e.target.value)}/>
                </Form.Group>
                <Form.Group>
                    <Button variant="outline-primary" type="submit" onClick={doSearch}>
                        <FaIcon icon="search"/> Search
                    </Button>
                </Form.Group>
            </Form>
        </Navbar>
    );
}

const CatalogTab = () => {
    return (
        <Pane noFixedHeight id="catalogTab">
            <Pane.Fixed>
                <SearchBar/>
            </Pane.Fixed>
            <Pane.Flex id="catalog-results">
                <div className="row w-100 h-100">
                    <div className="col left-pane h-100">
                        <TreeView/>
                    </div>
                    <div className="col right-pane h-100 px-0">
                        <CatalogEditor/>
                    </div>
                </div>
            </Pane.Flex>
        </Pane>
    );
}

export default CatalogTab;
