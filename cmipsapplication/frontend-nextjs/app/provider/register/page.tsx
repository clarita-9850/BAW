'use client';

import React, { useState } from 'react';
import apiClient from '@/lib/api';
import Link from 'next/link';

type StartRegistrationResponse = {
  registrationId: string;
  sessionId: string;
  currentStep?: number;
  email?: string;
  message?: string;
};

type UsernameCheckResponse = {
  available: boolean;
  suggestions?: string[];
};

type SecurityQuestionsResponse = {
  questions: string[];
};

type Step = 1 | 2 | 3 | 4 | 5 | 6;

export default function ProviderRegistrationPage() {
  const [step, setStep] = useState<Step>(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  const [registrationId, setRegistrationId] = useState<string | null>(null);

  // Step 1 – Identity
  const [providerNumber, setProviderNumber] = useState('');
  const [lastName, setLastName] = useState('');
  const [dob, setDob] = useState(''); // YYYY-MM-DD
  const [ssn4, setSsn4] = useState('');

  // Step 2 – Email
  const [email, setEmail] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [emailSent, setEmailSent] = useState(false);

  // Step 3 – Username
  const [username, setUsername] = useState('');
  const [usernameAvailable, setUsernameAvailable] = useState<boolean | null>(null);

  // Step 4 – Password
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Step 5 – Security Questions
  const [availableQuestions, setAvailableQuestions] = useState<string[] | null>(null);
  const [question1, setQuestion1] = useState('');
  const [question2, setQuestion2] = useState('');
  const [question3, setQuestion3] = useState('');
  const [answer1, setAnswer1] = useState('');
  const [answer2, setAnswer2] = useState('');
  const [answer3, setAnswer3] = useState('');

  const ensureRegistration = () => {
    if (!registrationId) {
      throw new Error('Registration session not found. Please start again.');
    }
    return registrationId;
  };

  const extractErrorMessage = (error: unknown, fallback: string): string => {
    if (typeof error === 'object' && error !== null) {
      const e = error as {
        response?: { data?: { error?: string; message?: string } };
        message?: string;
      };
      return e.response?.data?.error || e.response?.data?.message || e.message || fallback;
    }
    return fallback;
  };

  const handleStart = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      if (!providerNumber || !lastName || !dob || !ssn4) {
        setError('Please fill in all fields.');
        return;
      }
      const resp = await apiClient.post<StartRegistrationResponse>(
        '/esp/register/provider/start',
        {
        providerNumber,
        lastName,
        dateOfBirth: dob,
        ssn4,
        }
      );
      const data = resp.data;
      setRegistrationId(data.registrationId);
      setInfo('Identity validated. Please verify your email.');
      setStep(2);
    } catch (error: unknown) {
      setError(
        extractErrorMessage(
          error,
          'Failed to start registration. Please check your details.'
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSendEmailCode = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      const id = ensureRegistration();
      if (!email) {
        setError('Please enter your email.');
        return;
      }
      await apiClient.post(`/esp/register/${id}/send-verification`, { email });
      setEmailSent(true);
      setInfo('A verification code has been sent to your email.');
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to send verification code.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      const id = ensureRegistration();
      if (!verificationCode) {
        setError('Please enter the code sent to your email.');
        return;
      }
      await apiClient.post(`/esp/register/${id}/verify-email`, { code: verificationCode });
      setStep(3);
      setInfo('Email verified. Please choose a username.');
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to verify email code.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCheckUsername = async () => {
    setError(null);
    setInfo(null);
    if (!username) {
      setError('Enter a username first.');
      return;
    }
    try {
      const resp = await apiClient.get<UsernameCheckResponse>(
        '/esp/register/username/check',
        { params: { username } }
      );
      const available = !!resp.data.available;
      setUsernameAvailable(available);
      if (available) {
        setInfo('Username is available.');
      } else {
        setError('Username is already taken.');
      }
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to check username.')
      );
    }
  };

  const handleSetUsername = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      const id = ensureRegistration();
      if (!username) {
        setError('Please choose a username.');
        return;
      }
      await apiClient.post(`/esp/register/${id}/set-username`, { username });
      setStep(4);
      setInfo('Username saved. Please create your password.');
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to set username.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      const id = ensureRegistration();
      if (!password || !confirmPassword) {
        setError('Please enter and confirm your password.');
        return;
      }
      if (password !== confirmPassword) {
        setError('Passwords do not match.');
        return;
      }
      await apiClient.post(`/esp/register/${id}/set-password`, { password });
      // Load questions for next step
      const resp = await apiClient.get<SecurityQuestionsResponse>(
        '/esp/register/security-questions'
      );
      setAvailableQuestions(resp.data.questions || []);
      setStep(5);
      setInfo('Password saved. Please choose your security questions.');
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to set password.')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);
    setLoading(true);
    try {
      const id = ensureRegistration();
      if (!question1 || !question2 || !question3 || !answer1 || !answer2 || !answer3) {
        setError('Please choose three questions and provide answers.');
        return;
      }
      await apiClient.post(`/esp/register/${id}/complete`, {
        question1,
        answer1,
        question2,
        answer2,
        question3,
        answer3,
      });
      setStep(6);
      setInfo('Registration completed. You can now log in to ESP.');
    } catch (error: unknown) {
      setError(
        extractErrorMessage(error, 'Failed to complete registration.')
      );
    } finally {
      setLoading(false);
    }
  };

  const renderStepIndicator = () => (
    <div className="mb-6 flex justify-center gap-2 text-xs text-gray-600">
      {[1, 2, 3, 4, 5].map((s) => (
        <div
          key={s}
          className={`w-8 h-1 rounded-full ${
            step >= s ? 'bg-[#047857]' : 'bg-gray-300'
          }`}
        />
      ))}
    </div>
  );

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-xl bg-white rounded-lg shadow-lg p-8 mx-4">
        <div className="text-center mb-4">
          <h1 className="text-2xl font-bold text-gray-900">
            Provider ESP Registration
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Register to use the Electronic Services Portal (e-timesheets).
          </p>
        </div>

        {renderStepIndicator()}

        {error && (
          <div className="mb-4 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}
        {info && (
          <div className="mb-4 rounded-md bg-emerald-50 border border-emerald-200 px-3 py-2 text-sm text-emerald-800">
            {info}
          </div>
        )}

        {step === 1 && (
          <form onSubmit={handleStart} className="space-y-4">
            <h2 className="text-sm font-semibold text-gray-800 uppercase tracking-wide">
              Step 1: Verify Your Identity
            </h2>
            <p className="text-xs text-gray-600">
              Enter your provider information exactly as it appears in your IHSS
              enrollment paperwork.
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Provider Number
              </label>
              <input
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={providerNumber}
                onChange={(e) => setProviderNumber(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Last Name
              </label>
              <input
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Date of Birth
              </label>
              <input
                type="date"
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={dob}
                onChange={(e) => setDob(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Last 4 of SSN
              </label>
              <input
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                maxLength={4}
                value={ssn4}
                onChange={(e) => setSsn4(e.target.value)}
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800 disabled:bg-gray-400"
            >
              {loading ? 'Validating...' : 'Continue'}
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleVerifyEmail} className="space-y-4">
            <h2 className="text-sm font-semibold text-gray-800 uppercase tracking-wide">
              Step 2: Verify Your Email
            </h2>
            <p className="text-xs text-gray-600">
              We&apos;ll send a 6-digit code to your email address.
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Email Address
              </label>
              <input
                type="email"
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <button
              type="button"
              onClick={handleSendEmailCode}
              disabled={loading}
              className="w-full py-2 px-4 bg-gray-100 text-gray-800 rounded-md text-sm font-semibold border border-gray-300 hover:bg-gray-200 disabled:bg-gray-100"
            >
              {loading ? 'Sending...' : 'Send Verification Code'}
            </button>
            {emailSent && (
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Verification Code
                  </label>
                  <input
                    className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    maxLength={6}
                    required
                  />
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-2.5 px-4 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800 disabled:bg-gray-400"
                >
                  {loading ? 'Verifying...' : 'Verify Email'}
                </button>
              </div>
            )}
          </form>
        )}

        {step === 3 && (
          <form onSubmit={handleSetUsername} className="space-y-4">
            <h2 className="text-sm font-semibold text-gray-800 uppercase tracking-wide">
              Step 3: Create Username
            </h2>
            <p className="text-xs text-gray-600">
              Username must be 6–30 characters and can include letters,
              numbers, dot, underscore, or hyphen.
            </p>
            <div className="flex gap-2 items-center">
              <input
                className="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  setUsernameAvailable(null);
                }}
                required
              />
              <button
                type="button"
                onClick={handleCheckUsername}
                className="px-3 py-2 text-xs font-semibold border border-gray-300 rounded-md bg-gray-50"
              >
                Check
              </button>
            </div>
            {usernameAvailable === true && (
              <p className="text-xs text-emerald-700">Username is available.</p>
            )}
            {usernameAvailable === false && (
              <p className="text-xs text-red-700">Username is already taken.</p>
            )}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800 disabled:bg-gray-400"
            >
              {loading ? 'Saving...' : 'Save Username'}
            </button>
          </form>
        )}

        {step === 4 && (
          <form onSubmit={handleSetPassword} className="space-y-4">
            <h2 className="text-sm font-semibold text-gray-800 uppercase tracking-wide">
              Step 4: Create Password
            </h2>
            <p className="text-xs text-gray-600">
              Password must be at least 8 characters and include uppercase,
              lowercase, a number, and a special character.
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                type="password"
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Confirm Password
              </label>
              <input
                type="password"
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800 disabled:bg-gray-400"
            >
              {loading ? 'Saving...' : 'Save Password'}
            </button>
          </form>
        )}

        {step === 5 && availableQuestions && (
          <form onSubmit={handleComplete} className="space-y-4">
            <h2 className="text-sm font-semibold text-gray-800 uppercase tracking-wide">
              Step 5: Security Questions
            </h2>
            <p className="text-xs text-gray-600">
              Choose three different questions and provide answers you&apos;ll
              remember. Answers are not case-sensitive.
            </p>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Question 1
              </label>
              <select
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={question1}
                onChange={(e) => setQuestion1(e.target.value)}
                required
              >
                <option value="">Select a question</option>
                {availableQuestions.map((q) => (
                  <option key={q} value={q}>
                    {q}
                  </option>
                ))}
              </select>
              <input
                className="mt-2 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                placeholder="Answer"
                value={answer1}
                onChange={(e) => setAnswer1(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Question 2
              </label>
              <select
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={question2}
                onChange={(e) => setQuestion2(e.target.value)}
                required
              >
                <option value="">Select a question</option>
                {availableQuestions.map((q) => (
                  <option key={q} value={q}>
                    {q}
                  </option>
                ))}
              </select>
              <input
                className="mt-2 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                placeholder="Answer"
                value={answer2}
                onChange={(e) => setAnswer2(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Question 3
              </label>
              <select
                className="mt-1 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                value={question3}
                onChange={(e) => setQuestion3(e.target.value)}
                required
              >
                <option value="">Select a question</option>
                {availableQuestions.map((q) => (
                  <option key={q} value={q}>
                    {q}
                  </option>
                ))}
              </select>
              <input
                className="mt-2 w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                placeholder="Answer"
                value={answer3}
                onChange={(e) => setAnswer3(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800 disabled:bg-gray-400"
            >
              {loading ? 'Completing...' : 'Complete Registration'}
            </button>
          </form>
        )}

        {step === 6 && (
          <div className="space-y-4 text-center">
            <h2 className="text-lg font-semibold text-gray-900">
              Registration Complete
            </h2>
            <p className="text-sm text-gray-600">
              Your ESP account has been created. You can now log in to submit
              timesheets, view recipients, and manage your IHSS services.
            </p>
            <Link
              href="/login"
              className="inline-block mt-2 py-2.5 px-6 bg-emerald-700 text-white rounded-md text-sm font-semibold hover:bg-emerald-800"
            >
              Go to Login
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}


