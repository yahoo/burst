import React, {useEffect, useState} from "react";
import {Button, Form, Modal, Table} from "react-bootstrap";
import {Controlled as CodeMirror} from 'react-codemirror2'
import {useDispatch, useSelector} from "react-redux";

import {codeMirrorQueryOptions as options} from "../../utility/code-mirror";
import {Pane} from "../../components/pane";
import {actions as query} from "../../store/reducers/query"

const QueryLoadDialog = ({onHide}) => {
    const dispatch = useDispatch()
    const {allQueries} = useSelector(({query}) => ({
        allQueries: query.allQueries,
    }))
    const [selected, setSelected] = useState(null);
    const [language, setLanguage] = useState('');
    const [moniker, setMoniker] = useState('');
    useEffect(() => {
        dispatch(query.fetchQueries())
    }, [])
    const openQuery = q => {
        dispatch(query.setSavedQuery(q))
        onHide()
    }
    return (
        <Modal size="lg" show onHide={onHide} centered>
            <Modal.Header closeButton>
                <Modal.Title>Query List</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Pane>
                    <Pane.Flex>
                        <Table bordered hover striped>
                            <thead>
                            <tr>
                                <th>Language</th>
                                <th colSpan={2}>Query Name</th>
                            </tr>
                            <tr>
                                <th>
                                    <Form.Control as="select" value={language} onChange={e => setLanguage(e.target.value ?? '')}>
                                        <option>All</option>
                                        {['Eql', 'Hydra', 'Gist', 'Silq'].map(l => <option key={l} value={l}>{l}</option>)}
                                    </Form.Control>
                                </th>
                                <th colSpan={2}>
                                    <Form.Control value={moniker} onChange={e => setMoniker(e.target.value ?? '')}/>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {allQueries
                                .filter(q => !language || q.languageType === language)
                                .filter(q => !moniker || q.moniker?.includes(moniker))
                                .map(q => (
                                    <tr key={q.pk} onClick={() => setSelected(q)}>
                                        <td>{q.languageType}</td>
                                        <td>{q.moniker}</td>
                                        <td className="text-center">
                                            <Button variant="outline-success" size="sm" onClick={() => openQuery(q)}>Load</Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </Pane.Flex>
                    <Pane.Flex className="position-relative">
                        {selected
                            ? <CodeMirror value={selected.source} options={options}/>
                            : <div className="w-100 burst-empty-message">Click a row to preview query…</div>
                        }
                    </Pane.Flex>
                </Pane>
            </Modal.Body>
            {selected && (
                <Modal.Footer>
                    <Button variant="outline-secondary" onClick={() => openQuery(selected)}>Load</Button>
                </Modal.Footer>
            )}
        </Modal>
    )
}

export default QueryLoadDialog
