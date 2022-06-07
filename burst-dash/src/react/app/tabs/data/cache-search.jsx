import React, {Component} from "react";
import {Button, Col, Form, InputGroup, Row} from "react-bootstrap";

const nextOp = (op = '=') => {
    switch (op) {
        case '=':
            return '>';
        case '>':
            return '<';
        case '<':
            return '=';
        default:
            return '=';
    }
};

class SearchField extends Component {
    setValue = (e) => {
        const {update, name, value} = this.props;
        update({...value, name, value: e.target.value})
    };
    toggleOperator = () => {
        const {update, name, value} = this.props;
        update({...value, name, op: nextOp(value.op)});
    };

    render() {
        const {value: {op, value} = {}, type, placeholder} = this.props;
        return (
            <Col xs={2}>
                <Form.Group>
                    <InputGroup>
                        <InputGroup.Text>{placeholder}</InputGroup.Text>
                        <InputGroup.Text onClick={this.toggleOperator} className="pointer">
                            {op}
                        </InputGroup.Text>
                        <Form.Control type={type} placeholder="any" onChange={this.setValue} value={value}/>
                    </InputGroup>
                </Form.Group>
            </Col>
        );
    }
}

SearchField.defaultProps = {
    value: {op: '=', value: ''}
};

/**
 * The widget for searching generations
 */
class CacheSearch extends Component {
    constructor(props) {
        super(props);
        const {params = {}} = this.props;
        this.state = {
            showAdvanced: Object.keys(params).length > 0
        };
    }

    setDomain = (e) => {
        const {updateSearch, fetchViewsForDomain} = this.props;
        const domain = e.target.value;
        const updates = [
            {name: 'd', value: domain},
            {name: 'v'}
        ];
        if (domain && !domain.views) {
            fetchViewsForDomain(domain);
        }
        updateSearch(updates);
    };

    setView = (e) => {
        const {updateSearch} = this.props;
        updateSearch([{name: 'v', value: e.target.value}]);
    };


    setParam = (param) => {
        const {updateSearch} = this.props;
        updateSearch([param]);
    };

    toggleAdvancedSearch = () => {
        this.setState({showAdvanced: !this.state.showAdvanced});
    };

    doSearch = (e) => {
        e.preventDefault();
        const {fetchGenerations, d: domain, v: view, params} = this.props;
        fetchGenerations({domain, view, params});
    };

    render() {
        const {showAdvanced} = this.state;
        const {domains: allDomains = {}, views: allViews = {}} = this.props;
        const {d: domain, v: view, params} = this.props;
        const {
            i: itemCount, s: sliceCount, r: regionCount, b: byteCount,
            cla: coldLoadAt, clt: coldLoadTook, t: timeSkew, z: sizeSkew
        } = params;

        const domains = Object.values(allDomains);
        if (domain && !allDomains[Number(domain)]) {
            domains.push({pk: domain, moniker: `${domain} - Unknown`});
        }
        const views = Object.values(allViews).filter(v => !v || `${v.domainFk}` === `${domain}`);
        if (view && !allViews[view]) {
            views.push({pk: view, moniker: `${view} - Unknown`, domainFk: domain});
        }

        return (
            <Form onSubmit={this.doSearch} className="w-100 row">
                <Row className="mb-1 align-items-center">
                    <Form.Group as={Col} xs={2}>
                        <Form.Control as="select" value={domain} onChange={this.setDomain}>
                            <option value="">All Domains</option>
                            <optgroup label="Single Domain">
                                {domains.map(d => <option key={d.pk} value={d.pk}>{d.moniker}</option>)}
                            </optgroup>
                        </Form.Control>
                    </Form.Group>
                    <Form.Group as={Col} xs={2}>
                        <Form.Control as="select" value={view} onChange={this.setView}>
                            <option value="">All Views</option>
                            <optgroup label="Single View">
                                {views.map(v => <option key={v.pk} value={v.pk}>{v.moniker}</option>)}
                            </optgroup>
                        </Form.Control>
                    </Form.Group>
                    <Form.Group as={Col} xs={3} lg={2}>
                        <Button variant="outline-primary" size="sm" type="submit">
                            <span className="fa fa-refresh">{' '}Load Generations</span>
                        </Button>
                    </Form.Group>
                    <Form.Group as={Col} xs={3} lg={2} className="d-flex align-items-center">
                        <Button variant="link" size="sm"
                                onClick={this.toggleAdvancedSearch}>...</Button>
                    </Form.Group>
                </Row>
                <Row className={showAdvanced ? 'mb-1' : 'hide'}>
                    <Row className="mb-1">
                        <SearchField placeholder="Items" name="i" value={itemCount} update={this.setParam}/>
                        <SearchField placeholder="Slices" name="s" value={sliceCount} update={this.setParam}/>
                        <SearchField placeholder="Regions" name="r" value={regionCount} update={this.setParam}/>
                        <SearchField placeholder="Bytes" name="b" value={byteCount} update={this.setParam}/>
                    </Row>
                    <Row className="mb-1">
                        <SearchField placeholder="Load(c) At" name="cla" value={coldLoadAt} update={this.setParam}/>
                        <SearchField placeholder="Load(c) Took" name="clt" value={coldLoadTook}
                                     update={this.setParam}/>
                        <SearchField placeholder="Time Skew" name="t" value={timeSkew} update={this.setParam}/>
                        <SearchField placeholder="Size Skew" name="z" value={sizeSkew} update={this.setParam}/>
                    </Row>
                </Row>
            </Form>
        );
    }
}

export default CacheSearch
