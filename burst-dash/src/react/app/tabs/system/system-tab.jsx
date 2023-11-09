import React, {useEffect} from "react";
import {Pane} from "../../components/pane";
import {useDispatch, useSelector} from "react-redux";
import {actions, selectors} from '../../store/reducers/host';

const Row = ({label, value}) => (
    <tr>
        <td style={{width:'30%'}}>{label}</td>
        <td>{value || JSON.stringify(value)}</td>
    </tr>
)
const SystemTabFn = () => {
    const dispatch = useDispatch();
    const hostInfoRunning = useSelector(selectors.hostInfoRunning);
    const hostInfo = useSelector(selectors.hostInfo);
    const scalingInfo = useSelector(selectors.scalingInfo);
    const gitInfo = useSelector(selectors.gitInfo);

    useEffect(() => {
        dispatch(actions.startFetches())
    }, [hostInfoRunning])

    const { scalingInfo: scaleInfo, ...scaleConfig} = scalingInfo
    return (
        <Pane id="system">
            <Pane.Flex className={'mh-100'}>
                <h3>Scaling info</h3>
                <table>
                    <thead><Row label={<b>Config</b>}/></thead>
                    <tbody>
                    {Object.entries(scaleConfig || {}).map(([key, value]) =>
                        <Row key={key} label={key} value={value}/>
                    )}
                    </tbody>
                    <thead><Row label={<b>State</b>}/></thead>
                    <tbody>
                    {Object.entries(scaleInfo || {}).map(([key, value]) =>
                        <Row key={key} label={key} value={key === 'earliestAllowedRequestTime' ? new Date(value).toLocaleString() : value}/>
                    )}
                    </tbody>
                </table>
                <h3>System Info</h3>
                <table>
                    <tbody>
                    {Object.entries(hostInfo || {}).map(([key, value]) =>
                        <Row key={key} label={key} value={value}/>
                    )}
                    </tbody>
                </table>
                <h3>Build Info</h3>
                <table>
                    <tbody>
                    {Object.entries(gitInfo || {}).map(([key, value]) =>
                        <Row key={key} label={key} value={value}/>
                    )}
                    </tbody>
                </table>
            </Pane.Flex>
        </Pane>
    );
}

export default SystemTabFn
