import React, { useState, useMemo } from 'react';
import { useForm, SubmitHandler, FieldError } from 'react-hook-form';
import Button from '@mui/material/Button';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import OutlinedInput from '@mui/material/OutlinedInput';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import {
  isValueEmpty,
  valuesHaveErrors,
  emailValidationConfig,
  passwordValidationConfig,
  handleChange,
  login,
  parseJwt,
} from '../../utils/';
import { ErrorMessage} from '../../components/ErrorMessage';
import { useAuth, useLocalStorage } from '../../hooks';
import { SnackBar } from '../../components';
import { BAD_REQUEST, ERROR_MESSAGES } from '../../constants';
import { useNavigate } from 'react-router-dom';

interface LoginState {
  email: string;
  password: string;
  showPassword: boolean;
}

export interface LoginInputs {
  email: string;
  password: string;
}

const messageDuration = 2000;

const Login = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<LoginInputs>({
    criteriaMode: 'all',
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [token, setToken] = useLocalStorage('token');
  const [values, setValues] = useState<LoginState>({
    email: '',
    password: '',
    showPassword: false,
  });
  const { setIsAuthenticated } = useAuth();
  const [isError, setIsError] = useState<boolean>(false);
  const [isSubmiting, setIsSubmiting] = useState<boolean>(false);
  const [message, setMessage] = useState<string>('');
  const navigate = useNavigate();
  const isEmpty = isValueEmpty(values);
  const hasErrors = useMemo(() => valuesHaveErrors(errors), [errors]);

  const handleClickShowPassword = () => {
    setValues({
      ...values,
      showPassword: !values.showPassword,
    });
  };

  const handleMouseDownPassword = (
    event: React.MouseEvent<HTMLButtonElement>
  ) => {
    event.preventDefault();
  };

  const onChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    maxLength?: number
  ) => handleChange<LoginState>(event, setValues, maxLength);

  // src/pages/login/index.tsx

const onSubmit: SubmitHandler<LoginInputs> = async ({ email, password }) => {
  setIsSubmiting(true); // Disable the submit button

  try {
      const response = await login(email, password); // AWAIT the login!

      // If the login was successful, 'response' will contain { accessToken: "..." }
      const token = response.accessToken;
      setToken(token);          // Store the token (using your custom hook)
      localStorage.setItem('userId', parseJwt(token).sub); // Good practice
      setIsAuthenticated(true);   // Set authentication state
      setMessage("Login successful!");  // Optional success message
      navigate('/dashboard');    // Redirect to the dashboard

  } catch (error: any) {
      console.error("Login failed:", error);
      setIsError(true);

      // IMPROVED ERROR HANDLING (same as in createAnUser)
      if (error && typeof error === 'object' && 'error' in error) {
          setMessage(error.error); // Display the SPECIFIC error from the backend
      } else {
          setMessage(ERROR_MESSAGES.NOT_FOUND_USER); // Generic error message
      }

  } finally {
      setIsSubmiting(false); // Re-enable the submit button
  }
};

function parseJwt (token: string) { //Helper function
var base64Url = token.split('.')[1];
var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
}).join(''));

return JSON.parse(jsonPayload);
}

  return (
    <div
      className="tw-w-full tw-flex tw-flex-col tw-flex-1 tw-items-center tw-justify-center"
      style={{
        height: 'calc(100vh - 128px)',
      }}
    >
      <h2>Iniciar sesión</h2>
      <form
        className="tw-flex tw-flex-col tw-gap-y-12 tw-mt-10"
        onSubmit={handleSubmit(onSubmit)}
      >
        <div>
          <FormControl variant="outlined">
            <InputLabel htmlFor="outlined-adornment-password">
              Correo
            </InputLabel>
            <OutlinedInput
              id="outlined-adornment-email"
              type="text"
              value={values.email}
              {...register('email', emailValidationConfig)}
              onChange={onChange}
              label="email"
              autoComplete="off"
            />
          </FormControl>
          {errors.email && <ErrorMessage errors={errors.email as FieldError | undefined} />}
        </div>
        <div>
          <FormControl variant="outlined">
            <InputLabel htmlFor="outlined-adornment-password">
              Contraseña
            </InputLabel>
            <OutlinedInput
              id="outlined-adornment-password"
              type={values.showPassword ? 'text' : 'password'}
              value={values.password}
              {...register('password', passwordValidationConfig)}
              onChange={onChange}
              endAdornment={
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={handleClickShowPassword}
                    onMouseDown={handleMouseDownPassword}
                    edge="end"
                    className="tw-text-neutral-gray-100"
                  >
                    {values.showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              }
              label="Password"
              autoComplete="off"
            />
          </FormControl>
          {errors.password && (
            <ErrorMessage errors={errors.password as FieldError | undefined} />
          )}
        </div>
        <Button
          className={`tw-h-14 ${
            hasErrors || !isDirty || isEmpty || isSubmiting
              ? 'tw-text-neutral-gray-300 tw-border-neutral-gray-300 tw-cursor-not-allowed'
              : ''
          }`}
          type="submit"
          variant="outlined"
          disabled={hasErrors || !isDirty || isEmpty || isSubmiting}
        >
          Ingresar
        </Button>
      </form>
      {message.length > 0 && (
        <SnackBar
          duration={messageDuration}
          message={message}
          type={isError ? 'error' : 'primary'}
        />
      )}
    </div>
  );
};

export default Login;
