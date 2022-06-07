import React from 'react';
import cn from 'classnames'

const btnClass = "btn btn-outline-primary"
const TablePagination = ({pageIndex, pageCount, onPrevious, onNext, canPreviousPage, canNextPage, gotoPage}) => {
    const isFirst = !canPreviousPage
    const isLast = !canNextPage
    return <div className="d-flex align-items-center">
        {gotoPage && (
            <a
                className={cn(btnClass, 'mx-2', {disabled: isFirst})}
                onClick={() => gotoPage(0)}
                aria-disabled={isFirst}>
                {'<<'}
            </a>
        )}
        <a className={cn(btnClass, {disabled: isFirst})} onClick={onPrevious} aria-disabled={isFirst}>{'<'}</a>
        <span className="px-2">Page {pageIndex + 1} of {pageCount || 1}</span>
        <a className={cn(btnClass, {disabled: isLast})} onClick={onNext} aria-disabled={isLast}>{'>'}</a>
        {gotoPage && (
            <a
                className={cn(btnClass, 'mx-2', {disabled: isLast})}
                onClick={() => gotoPage(pageCount - 1)}
                aria-disabled={isLast}>
                {'>>'}
            </a>
        )}
    </div>;
}

export default TablePagination;
