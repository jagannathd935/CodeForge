import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Edit3, X } from 'lucide-react';

export const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [titleTagline, setTitleTagline] = useState('Computer Science Student');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    fetchProfile();
    const savedTagline = localStorage.getItem(`codeforge_tagline_${user?.id}`);
    if (savedTagline) setTitleTagline(savedTagline);
  }, [user]);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await api.get('/users/me/stats');
      const data = res.data.data;
      setProfile(data);
      setAvatarUrl(data?.avatarUrl || '');
    } catch (err) {
      console.error('Failed to load profile', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setUpdating(true);
    try {
      if (titleTagline) {
        localStorage.setItem(`codeforge_tagline_${user?.id}`, titleTagline);
      }
      const res = await api.put('/users/me', { avatarUrl });
      setProfile(res.data.data);
      setShowEditModal(false);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setUpdating(false);
    }
  };

  const initials = user?.username ? user.username.substring(0, 2).toUpperCase() : 'JD';

  const easy = profile?.easySolved !== undefined ? profile.easySolved : 20;
  const medium = profile?.mediumSolved !== undefined ? profile.mediumSolved : 12;
  const hard = profile?.hardSolved !== undefined ? profile.hardSolved : 3;
  const totalSolved = profile?.solvedCount !== undefined ? profile.solvedCount : (easy + medium + hard);
  const totalSubs = profile?.totalSubmissions !== undefined ? profile.totalSubmissions : 87;
  const streak = profile?.streak !== undefined ? profile.streak : 12;

  // Max for progress bars
  const maxCategory = Math.max(easy, medium, hard, 25);

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 font-sans">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">User Profile</h1>
        <p className="text-xs sm:text-sm text-gray-400 mt-1">Track your progress</p>
      </div>

      {/* Main Profile Card matching Interface 5 */}
      <div className="p-6 sm:p-8 rounded-2xl bg-[#0F172A]/90 border border-gray-800 shadow-2xl space-y-8">
        
        {/* Top: Avatar, Name, Handle, Tagline, Edit Profile */}
        <div className="flex flex-col sm:flex-row items-center sm:items-start justify-between gap-6 pb-6 border-b border-gray-800/80">
          <div className="flex flex-col sm:flex-row items-center gap-5 text-center sm:text-left">
            {/* Avatar Circle with JD / Initials */}
            {profile?.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                alt={user?.username}
                className="w-20 h-20 rounded-full object-cover border-2 border-blue-500/40 shadow-lg"
              />
            ) : (
              <div className="w-20 h-20 rounded-full bg-[#1E293B] border-2 border-gray-700 text-2xl font-bold text-gray-200 flex items-center justify-center shadow-lg font-mono">
                {initials}
              </div>
            )}

            <div className="space-y-1">
              <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                {user?.username ? user.username.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase()) : 'John Doe'}
              </h2>
              <p className="text-xs text-gray-400 font-mono">
                @{user?.username?.toLowerCase() || 'johndoe'}
              </p>
              <p className="text-xs text-gray-300 font-sans pt-0.5">
                {titleTagline || 'Computer Science Student'}
              </p>
            </div>
          </div>

          <button
            onClick={() => setShowEditModal(true)}
            className="px-4 py-2 rounded-lg bg-gray-900 hover:bg-gray-800 border border-gray-700 text-xs font-semibold text-gray-200 flex items-center gap-2 transition-colors"
          >
            <Edit3 className="w-3.5 h-3.5 text-gray-400" />
            <span>Edit Profile</span>
          </button>
        </div>

        {/* 3 Stats: Problems Solved, Total Submissions, Day Streak */}
        <div className="grid grid-cols-3 gap-4 text-center">
          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-white font-mono block">
              {totalSolved}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Problems Solved
            </span>
          </div>

          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-white font-mono block">
              {totalSubs}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Total Submissions
            </span>
          </div>

          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-white font-mono block">
              {streak}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Day Streak
            </span>
          </div>
        </div>

        {/* Difficulty Breakdown Progress Bars matching Interface 5 */}
        <div className="space-y-4 pt-4">
          {/* Easy */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold">
              <span className="text-emerald-400 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span>
                Easy
              </span>
              <span className="font-mono text-gray-300 font-bold">{easy}</span>
            </div>
            <div className="w-full h-2.5 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
              <div
                className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                style={{ width: `${Math.min(100, (easy / maxCategory) * 100)}%` }}
              ></div>
            </div>
          </div>

          {/* Medium */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold">
              <span className="text-amber-400 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-500 inline-block"></span>
                Medium
              </span>
              <span className="font-mono text-gray-300 font-bold">{medium}</span>
            </div>
            <div className="w-full h-2.5 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
              <div
                className="h-full rounded-full bg-amber-500 transition-all duration-500"
                style={{ width: `${Math.min(100, (medium / maxCategory) * 100)}%` }}
              ></div>
            </div>
          </div>

          {/* Hard */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold">
              <span className="text-rose-400 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-rose-500 inline-block"></span>
                Hard
              </span>
              <span className="font-mono text-gray-300 font-bold">{hard}</span>
            </div>
            <div className="w-full h-2.5 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
              <div
                className="h-full rounded-full bg-rose-500 transition-all duration-500"
                style={{ width: `${Math.min(100, (hard / maxCategory) * 100)}%` }}
              ></div>
            </div>
          </div>
        </div>

      </div>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4">
          <div className="w-full max-w-md bg-[#0D1117] border border-gray-800 rounded-2xl p-6 shadow-2xl relative">
            <button
              onClick={() => setShowEditModal(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-white mb-1">Edit Profile</h3>
            <p className="text-xs text-gray-400 mb-5">Update your tagline and avatar image URL</p>

            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">Title / Student Tagline</label>
                <input
                  type="text"
                  value={titleTagline}
                  onChange={(e) => setTitleTagline(e.target.value)}
                  placeholder="e.g. Computer Science Student"
                  className="w-full px-3.5 py-2 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">Avatar Image URL</label>
                <input
                  type="url"
                  value={avatarUrl}
                  onChange={(e) => setAvatarUrl(e.target.value)}
                  placeholder="https://... or leave empty to use initials circle"
                  className="w-full px-3.5 py-2 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-4 py-2 rounded-xl bg-gray-900 text-gray-300 text-xs font-semibold hover:bg-gray-800"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={updating}
                  className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-md disabled:opacity-50"
                >
                  {updating ? 'Saving...' : 'Save Profile'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
