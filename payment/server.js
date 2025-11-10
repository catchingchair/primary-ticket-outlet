import express from 'express';
import morgan from 'morgan';
import { v4 as uuid } from 'uuid';

const app = express();
const PORT = process.env.PORT || 9090;

app.use(express.json());
app.use(morgan('combined'));

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.post('/api/payments', (req, res) => {
  const { eventId, userEmail, amountCents, paymentToken } = req.body ?? {};
  if (!paymentToken) {
    return res.status(400).json({
      success: false,
      message: 'paymentToken is required',
    });
  }

  // In a real implementation, execute payment provider call here.
  res.json({
    success: true,
    reference: uuid(),
    message: 'Payment approved (simulated)',
    eventId,
    userEmail,
    amountCents,
  });
});

app.listen(PORT, () => {
  console.log(`Payment service listening on ${PORT}`);
});

