import React from "react";
import {useSelector} from "react-redux";
import {Controlled as CodeMirror} from "react-codemirror2";

import ProfilerResults from "./profiler-results";
import ProfilerExecutor from "./profiler-executor";
import {BurstWidget} from "../../layout/helpers";
import {codeMirrorQueryOptions as options} from "../../utility/code-mirror";

import './profiler-tab.scss';
import {Pane} from "../../components/pane";

const ProfilerSource = ({source, isLockedDown}) => (
    <div>
        <CodeMirror className={`profiler h-100 ${isLockedDown ? 'locked' : 'unlocked'}`}
                    value={source} options={{...options, readOnly: 'nocursor'}}/>
    </div>
)

const ProfilerTab = () => {
    const {
        queryText, selected, config, attached, running, requestPending, events
    } = useSelector(({query, profiler, crosscutting}) => ({
        attached: profiler.attached,
        running: profiler.running,
        requestPending: profiler.requestPending,
        config: profiler.config,
        events: profiler.events,
        selected: crosscutting.selectedDataset,
        queryText: query.text,

    }));
    const isLockedDown = !attached || running || requestPending;
    const source = running ? config.source : queryText;

    return (
        <Pane>
            <Pane.Fixed minHeight={0}>
                <div>The profiler is configured in Query</div>
            </Pane.Fixed>
            <Pane.Flex className="position-relative">
                <ProfilerSource source={source} isLockedDown={isLockedDown}/>
            </Pane.Flex>
            <Pane.Fixed overflow>
                <ProfilerExecutor isLockedDown={isLockedDown}/>
            </Pane.Fixed>
            <Pane.Flex>
                <ProfilerResults events={events}/>
            </Pane.Flex>
        </Pane>
    );
}

export default ProfilerTab
