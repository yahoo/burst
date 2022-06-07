const labels = [
    '-55min', '-50min', '-45min', '-40min', '-35min', '-30min', '-25min', '-20min', '-15min', '-10min', '-5min', 'Now'
];

export function averageValues(workers, prop) {
    const values = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    const counts = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    workers.forEach(({assessment}) => {
        if (!assessment) return;
        assessment[prop].history.forEach((pt, i) => {
            counts[i] = counts[i] + 1;
            values[i] = values[i] + pt.value;
        });
    });

    if (counts[0] === 0) {
        // no data yet
        return values;
    }

    while (counts[counts.length - 1] === 0) {
        values.unshift(values.pop());
        counts.unshift(counts.pop());
    }
    
    return values.map((v, i) => isNaN(v / counts[i]) ? 0 : (v / counts[i]).toFixed(2));
}

export function flattenData(workers, props) {
    return props.reduce((collector, prop) => {
        const data = averageValues(workers, prop);
        return collector.map((point, i) => {
            point[prop] = data[i];
            return point;
        });
    }, labels.map(label => ({label})));
}
