import React from "react";

import ConfigInfo from "./configInfo";

const Footer = () => (
    <div id="burst-footer">
        <div>
            &copy; Flurry Inc. 2017-{(new Date()).getFullYear()}
        </div>
        <ConfigInfo />
    </div>
);

export default Footer;
