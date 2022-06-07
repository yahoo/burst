import React, {useState} from "react";
import {elapsedTime} from "../../utility/burst-conversions";

const HoverDetails = ({hostname, begin, end, from, to, divider = " | "}) => (
    <>
        <b>{hostname}</b>
        <span className="me-4">{begin && `← ${begin} `} {begin && end && divider} {end && `${end} →`}</span>
        <span className="me-4">{elapsedTime(to - from)}</span>
    </>
);

const segmentForStage = ({stage, hostname, endMillis, totalDuration}) => {
    const {millis, args} = stage;
    const size = Math.max((100 * (endMillis - millis) / totalDuration), 0.5);

    switch (stage.name) {
        case "pre-start":
            return {
                name: 'pre-start',
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Request start"
                                     end="Particle dispatch"/>
            };
        case "start-wait":
            return {
                name: 'start-wait',
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Particle dispatch"
                                     end="Ack particle start"/>
            };
        case "Particle start":
            return {
                name: 'loading',
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Ack particle start"
                                     end="First store event"/>
            };
        case "Store event":
            return {
                name: `store-event ${args.join('-')}`,
                size,
                hover: <HoverDetails from={millis} to={endMillis} hostname={hostname} begin={args.join(' ')}/>
            };
        case "Data loaded":
            return {
                name: 'data-loaded',
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Last store event"
                                     end="Scan start"/>
            };
        case "Particle finish":
            return {
                name: 'scan',
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Scan start"
                                     end="Scan end"/>
            };
        case "end-wait":
            return {
                name: "end-wait",
                size,
                hover: <HoverDetails hostname={hostname} from={millis} to={endMillis} begin="Particle finished"/>
            }
    }
};

const Segment = ({segment: {name, size, hover}, setHover}) => (
    <div className={`flex-grow-1 ${name}`} style={{flexBasis: `${size}%`}} onMouseEnter={() => setHover(hover)}/>
)

const ParticleCell = ({beginMillis, particle, endMillis = Date.now(), expanded, setHover, setExpanded}) => {
    const {hostname} = particle;
    const totalDuration = endMillis - beginMillis;

    const segments = [];
    segments.push(segmentForStage({
        stage: {name: 'pre-start', millis: beginMillis},
        endMillis: particle.beginMillis,
        hostname,
        totalDuration
    }));

    let stage = {name: 'start-wait', millis: particle.beginMillis};
    if (particle.updates.length) {
        for (let i = 0; i < particle.updates.length; i++) {
            const nextStage = particle.updates[i];
            segments.push(segmentForStage({stage, hostname, endMillis: nextStage.millis, totalDuration}));
            stage = nextStage;
        }
        segments.push(segmentForStage({stage, hostname, endMillis: particle.endMillis, totalDuration}));
    } else {
        segments.push(segmentForStage({stage, hostname, endMillis, totalDuration}));
    }
    stage = {name: 'end-wait', millis: particle.endMillis};
    segments.push(segmentForStage({stage, hostname, endMillis, totalDuration}));

    return (
        <div className={`d-flex particle ${expanded ? 'expanded' : ''}`} onClick={setExpanded}>
            {segments.map((s, i) => <Segment key={i} segment={s} setHover={setHover}/>)}
        </div>
    )
}


export const ExecutionTimeline = ({execution}) => {
    const [hover, setHover] = useState(null);
    const [expanded, setExpanded] = useState({});

    return (
        <>
            <p style={{height: 25}}>{hover ? hover : 'Hover to see stage details. Click a row to expand'}</p>
            <div className="timeline" onMouseLeave={() => setHover(null)}>
                {execution.wave && execution.wave.particles.map(p => (
                        <ParticleCell key={p.ruid}
                                      expanded={expanded[p.ruid]}
                                      beginMillis={execution.beginMillis} endMillis={execution.endMillis} particle={p}
                                      setHover={setHover}
                                      setExpanded={() => setExpanded({...expanded, [p.ruid]: !expanded[p.ruid]})}/>
                    )
                )}
            </div>
        </>
    )
};
