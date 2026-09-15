import axios from 'axios'

const request = axios.create({
  baseURL: '/dev-api',
  timeout: 5_000,
})

export default request
