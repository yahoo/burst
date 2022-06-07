import React, {useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Col, Dropdown, DropdownButton, Form, Modal, Navbar, Row, Button} from "react-bootstrap";
import {actions as profiler} from "../../store/reducers/profiler";

const ProfilerExecutorFn = ({isLockedDown}) => {
    const dispatch = useDispatch()
    const [validationErr, setValidationErr] = useState('');
    const {
        running, attached, requestPending, config,
        source, timezone, selected
    } = useSelector(({profiler, query, crosscutting}) => ({
        running: profiler.running,
        attached: profiler.attached,
        requestPending: profiler.requestPending,
        config: profiler.config,
        source: query.text,
        timezone: query.timezone,
        selected: crosscutting.selectedDataset
    }))
    const domainPk = running ? config.domainPk : selected.domain;
    const viewPk = running ? config.viewPk : selected.view;
    const tz = running ? config.timezone : timezone;
    const onConcurrencySelect = value => dispatch(profiler.updateConfig({concurrency: value}))
    const onExecutionsSelect = value => dispatch(profiler.updateConfig({executions: value}))
    const onLoadsSelect = value => dispatch(profiler.updateConfig({reload: value}))
    const startStopProfiler = () => {
        if (running) {
            dispatch(profiler.stop())
        } else {
            const {concurrency, executions, reload} = config
            dispatch(profiler.start({
                domainPk: selected.domain, viewPk: selected.view,
                source, timezone, concurrency, executions, reload
            }))
        }
    }
    const toK = n => n > 999 ? `${n / 1000}k` : n

    return (
        <Navbar className="w-100">
            <Form className="row row-cols-lg-auto align-items-center">
                <Row>
                    <Col className="d-flex align-items-center">
                        <DropdownButton id="concurrency-dropdown" size="sm" variant="outline-primary"
                                        disabled={isLockedDown} title={`Concurrency [ ${config.concurrency} ]`}
                                        onSelect={onConcurrencySelect}>
                            {[1, 4, 8, 16, 32, 48].map(n => <Dropdown.Item key={n} eventKey={n}>{n}</Dropdown.Item>)}
                        </DropdownButton>
                    </Col>
                    <Col className="d-flex align-items-center">
                        <DropdownButton id="executions-dropdown" size="sm" variant="outline-primary"
                                        disabled={isLockedDown} title={`Executions [ ${toK(config.executions)} ]`}
                                        onSelect={onExecutionsSelect}>
                            {[1, 5, 10, 50, 100, 500, 1000, 5000, 10_000, 100_000].map(n => (
                                <Dropdown.Item key={n} eventKey={n}>{toK(n)}</Dropdown.Item>
                            ))}
                        </DropdownButton>
                    </Col>
                    <Col className="d-flex align-items-center">
                        <DropdownButton id="reload-every-dropdown" size="sm" variant="outline-primary"
                                        disabled={isLockedDown} title={`Reload Every [ ${toK(config.reload)} ]`}
                                        onSelect={onLoadsSelect}>
                            {[0, 1, 5, 10, 50, 100, 500, 1000, 5000, 10_000].map(n => (
                                <Dropdown.Item key={n} eventKey={n}>{toK(n)}</Dropdown.Item>
                            ))}
                        </DropdownButton>
                    </Col>
                    <Col>
                        <Form.Group as={Row} className="align-items-center">
                            <Form.Label column>Domain</Form.Label>
                            <Form.Control plaintext className="col" size="sm" readOnly value={`[ ${domainPk} ]`}/>
                        </Form.Group>
                    </Col>
                    <Col>
                        <Form.Group as={Row} className="align-items-center">
                            <Form.Label column>View</Form.Label>
                            <Form.Control plaintext className="col" size="sm" readOnly value={`[ ${viewPk} ]`}/>
                        </Form.Group>
                    </Col>
                    <Col>
                        <Form.Group as={Row} className="align-items-center">
                            <Form.Label column>TZ</Form.Label>
                            <Form.Control plaintext className="col" size="sm" readOnly value={`[ ${tz} ]`}/>
                        </Form.Group>
                    </Col>
                    <Col className="d-flex align-items-center justify-content-end">
                        <Button variant={`outline-${isLockedDown ? 'warning' : 'success'}`} disabled>
                            <span className={`fa ${isLockedDown ? 'fa-lock' : 'fa-unlock'}`}/>
                        </Button>
                    </Col>
                    <Col className="d-flex align-items-center">
                        <Button size="sm" variant={`outline-${running ? 'warning' : 'info'}`}
                                onClick={startStopProfiler} disabled={requestPending || !attached}>
                            {running ? 'Stop Profiler' : 'Profile'}
                            {running && <BarLoader/>}
                        </Button>
                    </Col>
                </Row>
            </Form>
            <Modal show={validationErr !== ''} onHide={() => setValidationErr('')} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>
                        <span>Invalid Parameters</span>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div>{validationErr}</div>
                </Modal.Body>
            </Modal>
        </Navbar>
    )
}

export default ProfilerExecutorFn
