// src/components/ErrorMessage/index.tsx
import React from 'react';
import { FieldError, FieldErrors, FieldValues, Path  } from 'react-hook-form';

export interface ErrorMessageProps<T extends FieldValues> {  // Generic type parameter
    errors?: FieldError | undefined;
    // OR for a more general type, use this
    // errors: FieldErrors<T>;
    // name: Path<T>; // if you intend to pass the field name
}
 //Type Guard
 function isFieldError(error: any): error is FieldError {
     return typeof error === 'object' && error !== null && 'message' in error && 'type' in error;
 }

export const ErrorMessage = <T extends FieldValues>({ errors}: ErrorMessageProps<T>) => { //Added Generic
  if (!errors) {
    return null;
  }
    // Use the type guard
    if (isFieldError(errors)) {
        return (
            <ul className="tw-flex tw-flex-col tw-gap-y-4 tw-pt-4 tw-bg-background">
                <li className="tw-text-error" key="error-message">
                    {errors.message}
                </li>
            </ul>
        );
    }

  // Handle the case where it's NOT a FieldError (optional, but good practice)
  console.warn("Unexpected error type:", errors); // Log the unexpected type
  return null; // Or some other fallback UI
};