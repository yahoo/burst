import "bootstrap/scss/bootstrap.scss";
import "font-awesome/css/font-awesome.css";
import "nprogress-npm/nprogress.css";
import "react-treeview/react-treeview.css";
import "react-table/react-table.css";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/neat.css";
import "codemirror/mode/clike/clike";
import "codemirror/mode/javascript/javascript";
import "codemirror/addon/edit/matchbrackets";
import "codemirror/addon/edit/trailingspace";
import "codemirror/addon/edit/closebrackets";
import "codemirror/addon/edit/closetag";
import "codemirror/addon/display/autorefresh";

import "core-js/actual";
import "regenerator-runtime/runtime";

import React, {useEffect, useState} from "react";
import ReactDOM from "react-dom";
import {Provider, useSelector} from "react-redux";
import {BrowserRouter as Router, NavLink, Redirect, Route, Switch} from "react-router-dom";
import {Nav} from "react-bootstrap";

import store from "./store";
import startWebSockets from './websockets';

import CatalogTab from "./tabs/catalog";
import QueryTab from "./tabs/query/query-tab";
import ExecutionTab from "./tabs/execution/execution-tab";
import DataTab from "./tabs/data/data-tab";
import BurnInTab from './tabs/burn-in/burn-in-tab';
import WorkerTab from "./tabs/worker/worker-tab";
import SettingsTab from "./tabs/settings/settings-tab";
import ThirftTab from './tabs/thrift/thrift-tab'
import SystemTab from "./tabs/system/system-tab";

import MessageBanner from "./layout/message-banner";
import Header from "./layout/header";
import Footer from "./layout/footer";
import {ErrorBoundary} from "./layout/helpers";

import "./burst.scss";

const renderFnCache = {};
const renderComponent = (Component, name) => {
    if (renderFnCache[name]) {
        return renderFnCache[name];
    }
    return renderFnCache[name] = ({match, location, history}) => (
        <ErrorBoundary key={match.path} name={name}>
            <Component match={match} location={location} history={history}/>
        </ErrorBoundary>
    );
};

const NavLinks = () => {
    const {running} = useSelector(({burnIn}) => ({
        running: burnIn.running
    }))
    const burnInTitle = running ? '🔥 Burn-In 🔥' : 'Burn-In'
    const navLinks = [
        {to: '/catalog', title: 'Catalog'},
        {to: '/query', title: 'Query'},
        {to: '/thrift', title: 'Thrift'},
        {to: '/waves', title: 'Waves'},
        {to: '/data', title: 'Datasets'},
        {to: '/workers', title: 'Workers'},
        {to: '/burn-in', title: burnInTitle},
        {to: '/settings', title: 'Settings'},
        {to: '/system', title: 'System'}
    ]

    return (
        <Nav className="w-100">
            {navLinks.map(link => (
                <NavLink key={link.title} className="nav-link nav-item"
                         to={link.to}>{link.title}</NavLink>
            ))}
        </Nav>
    )
}

/**
 *  The application chrome
 */
const App = () => {
    const [target, attachRef] = useState(null)
    useEffect(() => {
        startWebSockets()
    }, [])

    return (
        <Provider store={store}>
            <div className="min-h-100 b-flex-col">
                <div id="burst-header">
                    <Header/>
                </div>
                <Router basename={"/ui"}>
                    <>
                        <div id="burst-nav" ref={attachRef}>
                            <NavLinks/>
                            <MessageBanner target={target}/>
                        </div>
                        <div id="burst-content">
                            <Switch>
                                <Redirect exact from="/" to="/wave"/>
                                <Redirect exact from="/execution" to="/wave"/>
                                <Route path="/catalog" render={renderComponent(CatalogTab, "Catalog")}/>
                                <Route path="/query" render={renderComponent(QueryTab, "Query")}/>
                                <Route path="/waves" render={renderComponent(ExecutionTab, "Execution")}/>
                                <Route path="/thrift" render={renderComponent(ThirftTab, "Thrift")}/>
                                <Route path="/data/:generation?" render={renderComponent(DataTab, "Generation")}/>
                                <Route path="/burn-in" render={renderComponent(BurnInTab, "Burn-In")}/>
                                <Route path="/workers" render={renderComponent(WorkerTab, "Worker")}/>
                                <Route path="/settings" render={renderComponent(SettingsTab, "Settings")}/>
                                <Route path="/system" render={renderComponent(SystemTab, "System")}/>
                                <Redirect to="/waves"/>
                            </Switch>
                        </div>
                    </>
                </Router>
                <Footer/>
            </div>
        </Provider>
    )
}

ReactDOM.render(<App/>, document.getElementById('burstMasterBody'));
