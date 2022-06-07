import React, {useEffect} from "react";
import {Form} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";

import {actions as query} from '../../store/reducers/query';
import {actions as crosscutting} from '../../store/reducers/crosscutting';

const QueryParameters = () => {
    const dispatch = useDispatch()
    const {domains, views, timezone, timezones, selectedDataset: {domain, view}} = useSelector(({
                                                                                                    catalog,
                                                                                                    crosscutting,
                                                                                                    query
                                                                                                }) => ({
        domains: catalog.domains,
        views: catalog.views,
        timezone: query.timezone,
        timezones: query.timezones,
        selectedDataset: crosscutting.selectedDataset,
    }));
    useEffect(() => {
        const domainList = Object.values(domains)
        const viewList = Object.values(views)
        if (!domain && domainList.length) {
            const selectedDomain = domainList[0]
            const selectedView = viewList.filter(v => v.domainFk === selectedDomain.pk)?.[0]
            dispatch(crosscutting.selectDataset({domain: selectedDomain, view: selectedView}))
        } else if (!view && viewList.length) {
            const view = viewList.filter(v => v.domainFk === domain)?.[0];
            dispatch(crosscutting.selectDataset({view}))
        }
        if (timezones.length !== 0) {
            return;
        }
        dispatch(query.fetchTimezones())
    }, [])

    const domainList = Object.values(domains);
    const selectedDomain = domain || domainList?.[0]?.pk;
    const viewList = Object.values(views).filter(v => v.domainFk === selectedDomain);
    const selectedView = view || viewList?.[0]?.pk;
    const selectDomain = e => {
        const domain = domains[Number(e.target.value)];
        const view = Object.values(views).filter(v => v.domainFk === domain.pk)?.[0]
        dispatch(crosscutting.selectDataset({domain, view}))
    }
    const selectView = e => dispatch(crosscutting.selectDataset({view: views[e.target.value]}))
    const selectTz = e => dispatch(query.setTimezone(e.target.value))
    return (
        <Form className="row row-cols-lg-auto align-items-center burst-inline-form">
            <label>Domain</label>
            <Form.Group>
                <Form.Control bsPrefix="burst-form-control" as="select"
                              onChange={selectDomain} value={selectedDomain ?? ''}>
                    {domainList.map(d => <option key={d.pk} value={d.pk}>{d.moniker}</option>)}
                </Form.Control>
            </Form.Group>
            <label>View</label>
            <Form.Group>
                <Form.Control bsPrefix="burst-form-control" as="select"
                              onChange={selectView} value={selectedView ?? ''}>
                    {viewList.map(v => <option key={v.pk} value={v.pk}>{v.moniker}</option>)}
                </Form.Control>
            </Form.Group>
            <label>TZ</label>
            <Form.Group>
                <Form.Control bsPrefix="burst-form-control" as="select" style={{maxWidth: '1em'}}
                              onChange={selectTz} value={timezone || ''}>
                    <option key={'UTC'} value={'UTC'}>UTC</option>
                    <option key={'LOCAL'} value={'LOCAL'}>Local</option>
                    {timezones.map(tz => <option key={tz} value={tz}>{tz}</option>)}
                </Form.Control>
            </Form.Group>
        </Form>
    )
}

export default QueryParameters
