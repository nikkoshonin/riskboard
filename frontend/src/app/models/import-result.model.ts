export interface ImportError {
  lineNumber: number;
  rawLine: string;
  message: string;
}

export interface ImportResult {
  successCount: number;
  errorCount: number;
  errors: ImportError[];
}
