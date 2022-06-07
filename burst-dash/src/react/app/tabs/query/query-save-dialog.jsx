import React, {Component, useState} from 'react';
import {Button, ButtonToolbar, Col, Form, Modal} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";

import {actions as query} from '../../store/reducers/query'

const SaveQueryDialog = ({onHide}) => {
    const dispatch = useDispatch();
    const {text, savedQuery} = useSelector(({query}) => ({
        text: query.text,
        savedQuery: query.savedQuery,
    }))
    const [moniker, setMoniker] = useState(savedQuery.moniker);
    const [language, setLanguage] = useState(savedQuery.language);
    const onSave = e => {
        dispatch(query.createQuery({moniker, language, text}))
        onHide()
        e.preventDefault()
        return false;
    };

    return (
        <Modal show onHide={onHide} centered>
            <Modal.Header>
                <Modal.Title>Save Query</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form method="GET" onSubmit={onSave}>
                    <Form.Group as={Form.Row}>
                        <Form.Label column sm={2}>Name</Form.Label>
                        <Col sm={10}><Form.Control value={moniker} onChange={e => setMoniker(e.target.value)}/></Col>
                    </Form.Group>
                    <Form.Group as={Form.Row}>
                        <Form.Label column sm={2}>Language</Form.Label>
                        <Col sm={10}>
                            <Form.Control as="select" value={language} onChange={e => setLanguage(e.target.value)}>
                                {['Eql', 'Hydra'].map(l => <option key={l} value={l}>{l}</option>)}
                            </Form.Control>
                        </Col>
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <ButtonToolbar>
                    <Button variant="danger" size="sm" onClick={onHide}>Cancel</Button>
                    <Button variant="success" size="sm" onClick={onSave}>Save</Button>
                </ButtonToolbar>
            </Modal.Footer>
        </Modal>
    )
}

export default SaveQueryDialog;
