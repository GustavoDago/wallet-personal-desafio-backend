// src/pages/Dashboard/index.tsx
import React, { useEffect, useState } from 'react';
import Button from '@mui/material/Button';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ROUTES } from '../../constants';
import {
  CardCustom,
  Records,
  RecordVariant,
  Icon,
  SnackBar,
  IRecord,
  Skeleton,
  SkeletonVariant,
} from '../../components';
import {
  formatCurrency,
  getUserActivities,
  parseRecordContent,
  getAccount,
  sortByDate,
} from '../../utils/';
import { currencies, UNAUTHORIZED } from '../../constants/';
import { useUserInfo } from '../../hooks/useUserInfo'; // NEW: Use the corrected hook
import { Transaction, UserAccount } from '../../types/';
import { useAuth, useLocalStorage } from '../../hooks';
import { getUser } from '../../utils/api';

const numberOfActivities = 5;
const duration = 2000;

const Dashboard = () => {
  const { Argentina } = currencies;
  const { locales, currency } = Argentina;
  const navigate = useNavigate();
  const { user, loading, dispatch } = useUserInfo(); // Destructure dispatch
  const [token] = useLocalStorage('token');
  const [searchParams] = useSearchParams();
  const isSuccess = !!searchParams.get('success');
  const [userActivities, setUserActivities] = useState<IRecord[]>([]);
  const [userAccount, setUserAccount] = useState<UserAccount | null>(null); // Initialize as null
  const { logout } = useAuth();

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (storedToken && userId) {
      dispatch({ type: 'SET_LOADING', payload: true }); // Use dispatch for loading
      getUser(userId, storedToken)
        .then((userData) => {
          dispatch({ type: 'SET_USER', payload: userData });

          // Fetch activities and account information *after* getting user data
          Promise.all([
            getUserActivities(userId, storedToken),
            getAccount(userId, storedToken),
          ])
            .then(([activities, account]) => {
              if (activities.length > 0) {
                const orderedActivities = sortByDate(activities);
                const parsedRecords = orderedActivities.map(
                  (activity: Transaction) =>
                    parseRecordContent(activity, RecordVariant.TRANSACTION)
                );
                setUserActivities(parsedRecords);
              }

              if (account) {
                setUserAccount(account);
              }
              if (isSuccess) {
                setTimeout(() => navigate(ROUTES.HOME), duration);
              }
            })
            .catch((err) => {
              console.error('Error fetching activities or account', err);
              if (err.status === UNAUTHORIZED) {
                logout();
              }
            });
        })
        .catch((err) => {
          console.error('Error fetching user:', err);
          if (err.status === UNAUTHORIZED) {
            logout();
          }
          // Handle error appropriately (e.g., show error message, redirect to login)
        })
        .finally(() => {
          dispatch({ type: 'SET_LOADING', payload: false }); // Use dispatch for loading
        });
    } else {
      // No token, redirect to login
      navigate(ROUTES.LOGIN);
    }
  }, [navigate, isSuccess, logout, token]); // Correct dependencies

  if (loading) {
    return <div>Loading...</div>; // Or a more sophisticated loading indicator
  }

  if (!user) {
    return <div>Error: User data not available.</div>;
  }

  return (
    <div className="tw-w-full">
      <CardCustom
        content={
          <div className="tw-flex tw-justify-between tw-mb-8">
            <div>
              <p className="tw-mb-4 tw-font-bold">Dinero disponible</p>
              <p className="tw-text-xl tw-font-bold">
                {userAccount
                  ? formatCurrency(locales, currency, userAccount.balance)
                  : 'Loading...'}
              </p>
            </div>
            <div className="tw-flex tw-justify-between tw-items-start tw-flex-wrap tw-gap-x-4">
              <Link
                className="tw-underline hover:tw-text-primary"
                to={ROUTES.CARDS}
              >
                Ver tarjetas
              </Link>
              <Link
                className="tw-underline hover:tw-text-primary"
                to={ROUTES.PROFILE}
              >
                Ver CVU
              </Link>
            </div>
          </div>
        }
        actions={
          <>
            <Button
              onClick={() => navigate(ROUTES.LOAD_MONEY)}
              className="tw-h-12 tw-w-64"
              variant="outlined"
            >
              Ingresar dinero
            </Button>
            <Button
              onClick={() => navigate(ROUTES.SEND_MONEY)}
              className="tw-h-12 tw-w-64"
              variant="contained"
            >
              Transferir dinero
            </Button>
          </>
        }
      />

      <CardCustom
        content={
          <>
            <div>
              <p className="tw-mb-4 tw-font-bold">Tu actividad reciente</p>
            </div>
            {userActivities.length === 0 && !loading && (
              <p>No hay actividad registrada</p>
            )}
            {userActivities.length > 0 && (
              <Records records={userActivities} maxRecords={5} />
            )}

            {loading && (
              <Skeleton
                variant={SkeletonVariant.RECORD_LIST}
                numberOfItems={numberOfActivities}
              />
            )}
          </>
        }
        actions={
          userActivities.length === 0 && !loading ? null : (
            <Link
              to={ROUTES.ACTIVITY}
              className="tw-h-12 tw-w-full tw-flex tw-items-center tw-justify-between tw-mt-4 hover:tw-text-primary tw-px-4 hover:tw-bg-neutral-gray-500 tw-transition"
            >
              <span>Ver toda tu actividad</span>
              <Icon type="arrow-right" />
            </Link>
          )
        }
      />
      {isSuccess && (
        <SnackBar
          duration={duration}
          message="El dinero fue ingresado correctamente"
          type="success"
        />
      )}
    </div>
  );
};

export default Dashboard;