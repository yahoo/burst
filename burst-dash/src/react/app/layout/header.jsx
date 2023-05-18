import React from "react";
import cx from 'classnames';
import {useSelector} from "react-redux";

import './header.scss';
import HostInfo from "./hostInfo";
import {Col, Container, Row} from "react-bootstrap";

const Header = () => {
    const {show} = useSelector(({burnIn}) => ({show: burnIn.running}))
    return (
        <>
            <Container fluid>
                <Row className="align-items-center">
                    <Col xs={6} md={3}><img src="/static/images/burst.png" alt="Burst logo" height="40px"/></Col>
                    <Col xs={6} md={3}><span className={cx('pulse', 'burn-in-message', {hide: !show})}>🔥 Burn-In Running 🔥</span></Col>
                    <Col xs={12} md={6}><HostInfo/></Col>
                </Row>
            </Container>
        </>
    );
};

export default Header;
