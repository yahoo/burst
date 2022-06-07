import React, {Component} from "react";
import {connect} from "react-redux";

import './data-tab.scss';

import CacheSearch from "./cache-search";
import GenerationSummary from "./generation-summary";
import GenerationsList from "./generations-list";
import {fetchDomains, fetchViewsForDomain} from "../../store/reducers/catalog";
import {evictGeneration, fetchSlices, flushGeneration, genKeyString, searchGenerations} from "../../store/reducers/data";
import {Pane} from "../../components/pane";

const queryToSearch = ({search}) => {
    return search.substring(1).split('&').reduce((args, item) => {
        const [key, val] = item.split('=');
        switch (key) {
            case 'params':
                item.substr("params=".length).split(',').forEach(p => {
                    const [name, op, value] = p.split('-');
                    args.params[name] = {name, op, value};
                });
                break;
            default:
                args[key] = val;
        }
        return args;
    }, {params: {}});
};

/**
 * Browser component for the Fabric distributed data cache
 */
class DataTab extends Component {
    constructor(props) {
        super(props);
        const {location} = props;
        const {d: domain, v: view, params} = queryToSearch(location);
        this.props.fetchGenerations({domain, view, params});
    }

    selectGeneration = ({domainKey, viewKey, generationClock}) => {
        const {location, history, fetchSlices} = this.props;
        fetchSlices({domainKey, viewKey, generationClock});
        history.replace({...location, pathname: `/data/${genKeyString({domainKey, viewKey, generationClock})}`})
    };

    setSearchArgument = (updates) => {
        const {location, history} = this.props;
        const newArgs = queryToSearch(location);
        updates.forEach(({name, value, op = '='}) => {
            if (name === 'd' || name === 'v') {
                if (value) {
                    newArgs[name] = value;
                } else {
                    delete newArgs[name];
                }
                return;
            }
            newArgs.params[name] = {name: name, op, value: value};
        });

        const queryParams = Object.entries(newArgs)
            .filter(kv => kv[0] === 'params' ? Object.keys(kv[0]) : kv[1])
            .map(kv => {
                if (kv[0] === 'params') {
                    const params = Object.values(kv[1]);
                    return params.length ? `params=${params.map(p => `${p.name}-${p.op}-${p.value}`).join(',')}` : '';
                }
                return `${kv[0]}=${kv[1]}`;
            });

        const search = `?${queryParams.join('&')}`;
        history.replace({...location, search});
    };

    render() {
        const {generations, domains, views, slices, match, location} = this.props;
        const {fetchViewsForDomain, fetchGenerations, flushGeneration, evictGeneration} = this.props;
        const searchArgs = queryToSearch(location);

        return (
            <Pane id="data-tab" noFixedHeight>
                <Pane.Fixed>
                    <CacheSearch domains={domains} views={views} {...searchArgs}
                                 updateSearch={this.setSearchArgument}
                                 fetchViewsForDomain={fetchViewsForDomain}
                                 fetchGenerations={fetchGenerations}/>
                </Pane.Fixed>
                <Pane.Fixed minHeight={generations.length ? 120 : 75}>
                    <GenerationSummary generations={generations}/>
                </Pane.Fixed>
                <Pane.Flex>
                    <GenerationsList generations={generations} slices={slices}
                                     onSelect={this.selectGeneration}
                                     flushGeneration={flushGeneration}
                                     evictGeneration={evictGeneration}/>
                </Pane.Flex>
            </Pane>
        )
    }
}

const connectStateToProps = ({catalog: {domains, views}, data: {generations, slices}}) => ({
    domains, views, generations, slices
});

const mapDispatchToProps = dispatch => ({
    fetchDomains: () => dispatch(fetchDomains()),
    fetchViewsForDomain: domain => dispatch(fetchViewsForDomain(domain)),
    fetchGenerations: ({domainKey, viewKey, params}) => dispatch(searchGenerations({domainKey, viewKey, params})),
    fetchSlices: ({domainKey, viewKey, generationClock}) => dispatch(fetchSlices({domainKey, viewKey, generationClock})),
    flushGeneration: (identity) => dispatch(flushGeneration(identity)),
    evictGeneration: (identity) => dispatch(evictGeneration(identity)),
});

DataTab = connect(connectStateToProps, mapDispatchToProps)(DataTab);

export default DataTab
