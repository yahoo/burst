import React, {useState} from "react";

import {Button} from "react-bootstrap";

export const ToggleItem = ({title = '', show = false, children}) => {
    const [expanded, setExpanded] = useState(show);
    return (
        <div>
            <div className="toggle-header">
                <b>{title}</b>
                <Button variant="link" onClick={() => setExpanded(!expanded)}>({expanded ? "Hide" : "Show"})</Button>
            </div>
            {expanded ? children : null}
        </div>
    );
};
