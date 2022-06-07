import React from "react";
import PropTypes from 'prop-types';
import {connect} from "react-redux";
import WorkerTable from "./worker-table";
import {Pane} from "../../components/pane";

import './workers.scss'

const SummaryField = ({name, summary}) => <span className={name}>{name}: {summary[name] || 0}</span>;

class WorkerTab extends React.Component {
    render() {
        const {workers, meta} = this.props;
        const {summary} = workers;
        return (
            <Pane id="workers" noFixedHeight>
                <Pane.Fixed>
                    <div className="node-summary">
                        <span className="total">Total Workers: {summary.count}</span>
                        <SummaryField name="Live" summary={summary}/>
                        <SummaryField name="Tardy" summary={summary}/>
                        <SummaryField name="Flaky" summary={summary}/>
                        <SummaryField name="Dead" summary={summary}/>
                        <SummaryField name="Exiled" summary={summary}/>
                        <SummaryField name="Unknown" summary={summary}/>
                    </div>
                </Pane.Fixed>
                <Pane.Flex>
                    <WorkerTable workers={workers} meta={meta}/>
                </Pane.Flex>
            </Pane>
        );
    }
}

WorkerTab.contextTypes = {
    store: PropTypes.object,
};

const mapStateToProps = ({workerTab}) => {
    const {workers, meta} = workerTab;
    return {workers, meta};
};

export {WorkerTab};

export default connect(mapStateToProps)(WorkerTab);
