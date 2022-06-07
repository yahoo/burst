import React from "react";
import cx from 'classnames';
import {connect} from "react-redux";

import './header.scss';
import HostInfo from "./hostInfo";
import {Container, Row, Col} from "react-bootstrap";

const Header = ({show = false}) => (
    <>
        <Container fluid>
            <Row className="align-items-center">
                <Col xs={6} md={3}><img src="/static/images/burst.png" alt="Burst logo" height="40px"/></Col>
                <Col xs={6} md={3}><span className={cx('pulse', 'torcher-message', {hide: !show})}>Torcher Running</span></Col>
                <Col xs={12} md={6}><HostInfo/></Col>
            </Row>
        </Container>
    </>
);

const connectStateToProps = ({torcherTab}) => ({
    show: torcherTab.status.running
});

export default connect(connectStateToProps)(Header);
