import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
    const isDark = ref(false)

    function initTheme() {
        const savedTheme = localStorage.getItem('theme')
        if (savedTheme === 'dark') {
            isDark.value = true
            document.documentElement.setAttribute('data-theme', 'dark')
        } else {
            isDark.value = false
            document.documentElement.removeAttribute('data-theme')
        }
    }

    function toggleTheme() {
        isDark.value = !isDark.value
        if (isDark.value) {
            document.documentElement.setAttribute('data-theme', 'dark')
            localStorage.setItem('theme', 'dark')
        } else {
            document.documentElement.removeAttribute('data-theme')
            localStorage.setItem('theme', 'light')
        }
    }

    function setTheme(dark) {
        isDark.value = dark
        if (dark) {
            document.documentElement.setAttribute('data-theme', 'dark')
            localStorage.setItem('theme', 'dark')
        } else {
            document.documentElement.removeAttribute('data-theme')
            localStorage.setItem('theme', 'light')
        }
    }

    return {
        isDark,
        initTheme,
        toggleTheme,
        setTheme
    }
})
