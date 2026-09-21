import React from 'react';
import { CheckCircle2, XCircle, Clock, AlertTriangle, Cpu } from 'lucide-react';

export const DifficultyBadge = ({ difficulty }) => {
  const diff = difficulty?.toUpperCase() || 'EASY';

  if (diff === 'EASY') {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-300">
        Easy
      </span>
    );
  }
  if (diff === 'MEDIUM') {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-300">
        Medium
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-300">
      Hard
    </span>
  );
};

export const StatusBadge = ({ status }) => {
  switch (status) {
    case 'ACCEPTED':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-300">
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
          Accepted
        </span>
      );
    case 'WRONG_ANSWER':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-rose-50 text-rose-700 border border-rose-300">
          <XCircle className="w-3.5 h-3.5 text-rose-600" />
          Wrong Answer
        </span>
      );
    case 'TIME_LIMIT_EXCEEDED':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-amber-50 text-amber-700 border border-amber-300">
          <Clock className="w-3.5 h-3.5 text-amber-600" />
          Time Limit Exceeded
        </span>
      );
    case 'COMPILATION_ERROR':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-rose-50 text-rose-700 border border-rose-300">
          <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
          Compilation Error
        </span>
      );
    case 'RUNTIME_ERROR':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-purple-50 text-purple-700 border border-purple-300">
          <Cpu className="w-3.5 h-3.5 text-purple-600" />
          Runtime Error
        </span>
      );
    case 'RUNNING':
    case 'PENDING':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-blue-50 text-blue-700 border border-blue-300">
          <div className="w-2.5 h-2.5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
          Evaluating
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-300">
          {status}
        </span>
      );
  }
};

