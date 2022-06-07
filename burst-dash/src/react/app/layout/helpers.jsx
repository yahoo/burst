import React, {Component} from "react";

export class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = {
            hasError: false
        };
    }

    componentDidCatch(error, info) {
        this.setState({hasError: true, error, info});
    }

    render() {
        if (this.state.hasError) {
            const {error, info} = this.state;
            return (
                <div className="b-flex-col">
                    <h1>Unhandled error occurred in {this.props.name}</h1>
                    <p id="boundry-message">{error.message || 'No Message'}{'\n'}</p>
                    <pre id="boundry-stack">
                        {error.stack || 'No stacktrace'}{'\n\n'}
                        <b>From component:</b>
                        {info.componentStack}
                    </pre>
                </div>
            );
        }

        return this.props.children;
    }
}

export const DownloadLink = ({json, filename, text = 'Save as json'}) => {
    const download = async e => {
        e.preventDefault();
        const data = JSON.stringify(json, null, 2);
        const href = URL.createObjectURL(new Blob([data], {type: 'application/json'}));
        const link = document.createElement('a');
        link.href = href;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    return (
        <a href="" onClick={download}>{text}</a>
    )
};
